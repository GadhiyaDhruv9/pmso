package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.Service.ProjectService;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.ProjectAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserRepository userRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;

    public DashboardController(ProjectService projectService, TaskService taskService,
                               UserRepository userRepository, ProjectAssignmentRepository projectAssignmentRepository) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getDashboard(Authentication auth) {
        try {
            String username = auth.getName();
            logger.info("Fetching dashboard for: {}", username);

            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            List<ProjectDto> projects;
            if (isAdmin) {
                projects = projectService.getAllProjects();
            } else {
                projects = projectAssignmentRepository.findByUser_UserId(user.getUserId())
                        .stream()
                        .map(assignment -> {
                            try {
                                return projectService.getProjectById(assignment.getProject().getProjectId());
                            } catch (Exception e) {
                                logger.warn("Error fetching project {}", assignment.getProject().getProjectId());
                                return null;
                            }
                        })
                        .filter(project -> project != null)
                        .collect(Collectors.toList());
            }

            Map<String, List<ProjectDto>> projectsByStatus = projects.stream()
                    .collect(Collectors.groupingBy(
                            project -> project.getProjectStatus() != null ? project.getProjectStatus() : "Draft"
                    ));

            //PROJECT DATA
            List<Map<String, Object>> projectDetails = new ArrayList<>();
            for (Map.Entry<String, List<ProjectDto>> entry : projectsByStatus.entrySet()) {
                String status = entry.getKey();
                List<ProjectDto> statusProjects = entry.getValue();

                List<Map<String, Object>> projectArray = statusProjects.stream()
                        .map(project -> {
                            Map<String, Object> projectData = new HashMap<>();

                            ProjectDto projectDtoWithoutTasks = getProjectDto(project);

                            projectData.put("projectDetails", projectDtoWithoutTasks);

                            List<TaskDto> projectTasks;
                            try {
                                if (isAdmin) {
                                    projectTasks = taskService.getTasksByProject(project.getProjectId());
                                } else {
                                    projectTasks = taskService.getTasksByAssignedUser(username).stream()
                                            .filter(task -> task.getProjectId().equals(project.getProjectId()))
                                            .collect(Collectors.toList());
                                }

                            } catch (Exception e) {
                                logger.warn("Error fetching tasks for project {}", project.getProjectId());
                                projectTasks = new ArrayList<>();
                            }

                            Map<String, List<TaskDto>> tasksByStatus = projectTasks.stream()
                                    .collect(Collectors.groupingBy(
                                            task -> task.getTaskStatus() != null ? task.getTaskStatus() : "Draft"
                                    ));

                            //TASK DATA
                            List<Map<String, Object>> taskStatusData = new ArrayList<>();
                            for (Map.Entry<String, List<TaskDto>> taskEntry : tasksByStatus.entrySet()) {
                                String taskStatus = taskEntry.getKey();
                                List<TaskDto> statusTasks = taskEntry.getValue();

                                Map<String, Object> taskStatusInfo = new HashMap<>();
                                taskStatusInfo.put("taskStatus", taskStatus);
                                taskStatusInfo.put("taskCount", statusTasks.size());
                                taskStatusInfo.put("tasks", statusTasks);

                                taskStatusData.add(taskStatusInfo);
                            }

                            projectData.put("totalTask", projectTasks.size());
                            projectData.put("taskDetails", taskStatusData);
                            return projectData;
                        })
                        .collect(Collectors.toList());

                Map<String, Object> statusData = new HashMap<>();
                statusData.put("projectStatus", status);
                statusData.put("projectCount", statusProjects.size());
                statusData.put("project", projectArray);

                projectDetails.add(statusData);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("totalProjects", projects.size());
            data.put("role", isAdmin ? "ADMIN" : "USER");
            data.put("projectDetails", projectDetails);

            logger.info("Dashboard retrieved successfully for user: {}", username);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dashboard retrieved successfully");
            response.put("data", data);
            response.put("error", null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching dashboard for {}: {}", auth.getName(), e.getMessage());
            return ResponseUtil.fail("Failed to fetch dashboard: " + e.getMessage(),
                    null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static ProjectDto getProjectDto(ProjectDto project) {
        ProjectDto projectDtoWithoutTasks = new ProjectDto();
        projectDtoWithoutTasks.setProjectId(project.getProjectId());
        projectDtoWithoutTasks.setProjectName(project.getProjectName());
        projectDtoWithoutTasks.setProjectType(project.getProjectType());
        projectDtoWithoutTasks.setProjectStatus(project.getProjectStatus());
        projectDtoWithoutTasks.setProjectDescription(project.getProjectDescription());
        projectDtoWithoutTasks.setCreatedAt(project.getCreatedAt());
        projectDtoWithoutTasks.setUpdatedAt(project.getUpdatedAt());
        projectDtoWithoutTasks.setCreatedByUsername(project.getCreatedByUsername());
        projectDtoWithoutTasks.setUpdatedByUsername(project.getUpdatedByUsername());
        projectDtoWithoutTasks.setAssignedToUsernames(project.getAssignedToUsernames());
        projectDtoWithoutTasks.setTasks(null);
        return projectDtoWithoutTasks;
    }
}