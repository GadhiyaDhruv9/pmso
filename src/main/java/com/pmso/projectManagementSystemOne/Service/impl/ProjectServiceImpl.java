package com.pmso.projectManagementSystemOne.Service.impl;

import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.entity.Project;
import com.pmso.projectManagementSystemOne.entity.ProjectAssignment;
import com.pmso.projectManagementSystemOne.entity.ProjectDetails;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.enums.Status;
import com.pmso.projectManagementSystemOne.exception.InvalidStatusTransitionException;
import com.pmso.projectManagementSystemOne.mapper.ProjectMapper;
import com.pmso.projectManagementSystemOne.repository.ProjectAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.ProjectDetailsRepository;
import com.pmso.projectManagementSystemOne.repository.ProjectRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.Service.ProjectService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;
    private final ProjectDetailsRepository projectDetailsRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              UserRepository userRepository,
                              ProjectAssignmentRepository projectAssignmentRepository,
                              ProjectDetailsRepository projectDetailsRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.projectDetailsRepository = projectDetailsRepository;
    }

    @Transactional
    public ProjectDto createProject(ProjectDto projectDto, String username) {
        logger.info("Creating project for user: {}", username);
        try {
            UserEntity creator = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Project project = ProjectMapper.mapToProject(projectDto);
            project.setCreatedBy(creator);
            project.setUpdatedBy(creator);
            Project savedProject = projectRepository.save(project);
            ProjectDetails projectDetails = new ProjectDetails();
            projectDetails.setProject(savedProject);
            projectDetailsRepository.save(projectDetails);

            logger.info("Project {} created successfully by {}", savedProject.getProjectName(), username);
            return ProjectMapper.mapToProjectDto(savedProject);
        } catch (Exception e) {
            logger.error("Error creating project: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return ProjectMapper.mapToProjectDto(project);
    }

    @Override
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::mapToProjectDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto projectDto, String username) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Convert string status to enum
        Status newStatus = projectDto.getProjectStatus() != null ?
                Status.valueOf(projectDto.getProjectStatus().replace(" ", "_")) :
                Status.Draft;

        // Validate status transition
        Status currentStatus = project.getProjectStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition project status from %s to %s. " +
                                    "Valid transitions are: Draft → Pending → In_Progress → Completed",
                            currentStatus, newStatus));
        }

        // Authorization check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean isAdminOrManager = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_MANAGER"));
            if (!isAdminOrManager && !project.getCreatedBy().getUsername().equals(username)) {
                throw new RuntimeException("Only the creator or admin/manager can update this project");
            }
        } else if (!project.getCreatedBy().getUsername().equals(username)) {
            throw new RuntimeException("Only the creator can update this project");
        }

        UserEntity updater = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        project.setProjectName(projectDto.getProjectName());
        project.setProjectType(projectDto.getProjectType());
        project.setProjectStatus(newStatus); // Use the converted enum value
        project.setProjectDescription(projectDto.getProjectDescription());
        project.setUpdatedBy(updater);
        Project updatedProject = projectRepository.save(project);

        return ProjectMapper.mapToProjectDto(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, String username) {
        logger.info("Attempting to delete project {} by user: {}", projectId, username);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean isAdminOrManager = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_MANAGER"));
            if (!isAdminOrManager && !project.getCreatedBy().getUsername().equals(username)) {
                throw new RuntimeException("Only the creator or admin/manager can delete this project");
            }
        } else if (!project.getCreatedBy().getUsername().equals(username)) {
            throw new RuntimeException("Only the creator can delete this project");
        }

        projectDetailsRepository.deleteByProject_ProjectId(projectId);
        projectRepository.delete(project);
        logger.info("Project {} and its tasks deleted successfully", projectId);
    }

    @Override
    @Transactional
    public void assignUserToProject(Long projectId, Long userId, String role) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserEntity creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setProject(project);
        assignment.setUser(user);
        assignment.setRole(role);
        assignment.setCreatedBy(creator);
        assignment.setUpdatedBy(creator);
        projectAssignmentRepository.save(assignment);
    }
}