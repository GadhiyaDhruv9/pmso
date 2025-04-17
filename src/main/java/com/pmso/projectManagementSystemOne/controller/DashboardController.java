package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.Service.ProjectService;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import com.pmso.projectManagementSystemOne.entity.ProjectAssignment;
import com.pmso.projectManagementSystemOne.entity.TaskAssignment;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.ProjectAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.TaskAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    public DashboardController(
            ProjectService projectService,
            TaskService taskService,
            UserRepository userRepository,
            ProjectAssignmentRepository projectAssignmentRepository,
            TaskAssignmentRepository taskAssignmentRepository) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserDashboard(Authentication auth) {
        try {
            String username = auth.getName();
            logger.info("Fetching dashboard for user: {}", username);

            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get projects assigned to the user
            List<ProjectAssignment> projectAssignments = projectAssignmentRepository.findAll().stream()
                    .filter(assignment -> assignment.getUser().getUserId().equals(user.getUserId()))
                    .collect(Collectors.toList());
            List<ProjectDto> assignedProjects = projectAssignments.stream()
                    .map(assignment -> projectService.getProjectById(assignment.getProject().getProjectId()))
                    .collect(Collectors.toList());

            // Get tasks assigned to the user
            List<TaskAssignment> taskAssignments = taskAssignmentRepository.findByUser_Username(username);
            List<TaskDto> assignedTasks = taskAssignments.stream()
                    .map(assignment -> taskService.getTaskById(assignment.getTask().getTaskId()))
                    .collect(Collectors.toList());

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("projects", assignedProjects);
            dashboardData.put("tasks", assignedTasks);
            dashboardData.put("user", username);

            logger.info("User dashboard retrieved successfully for user: {}", username);
            return ResponseUtil.success("User dashboard retrieved successfully", dashboardData);
        } catch (Exception e) {
            logger.error("Error fetching user dashboard for {}: {}", auth.getName(), e.getMessage());
            return ResponseUtil.fail("Failed to fetch user dashboard", null, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            logger.info("Fetching admin dashboard");

            // Get all projects
            List<ProjectDto> allProjects = projectService.getAllProjects();

            // Get project assignments with user details
            List<Map<String, Object>> projectAssignments = projectAssignmentRepository.findAll().stream()
                    .map(assignment -> {
                        Map<String, Object> assignmentData = new HashMap<>();
                        assignmentData.put("projectId", assignment.getProject().getProjectId());
                        assignmentData.put("projectName", assignment.getProject().getProjectName());
                        assignmentData.put("projectStatus", assignment.getProject().getProjectStatus());
                        assignmentData.put("userId", assignment.getUser().getUserId());
                        assignmentData.put("username", assignment.getUser().getUsername());
                        assignmentData.put("role", assignment.getRole());
                        return assignmentData;
                    })
                    .collect(Collectors.toList());

            // Get all tasks
            List<TaskDto> allTasks = allProjects.stream()
                    .flatMap(project -> taskService.getTasksByProject(project.getProjectId()).stream())
                    .collect(Collectors.toList());

            // Get task assignments with user details
            List<Map<String, Object>> taskAssignments = taskAssignmentRepository.findAll().stream()
                    .map(assignment -> {
                        Map<String, Object> assignmentData = new HashMap<>();
                        assignmentData.put("taskId", assignment.getTask().getTaskId());
                        assignmentData.put("taskName", assignment.getTask().getTaskName());
                        assignmentData.put("taskStatus", assignment.getTask().getTaskStatus());
                        assignmentData.put("projectId", assignment.getTask().getProject().getProjectId());
                        assignmentData.put("userId", assignment.getUser().getUserId());
                        assignmentData.put("username", assignment.getUser().getUsername());
                        return assignmentData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("projects", allProjects);
            dashboardData.put("projectAssignments", projectAssignments);
            dashboardData.put("tasks", allTasks);
            dashboardData.put("taskAssignments", taskAssignments);

            logger.info("Admin dashboard retrieved successfully");
            return ResponseUtil.success("Admin dashboard retrieved successfully", dashboardData);
        } catch (Exception e) {
            logger.error("Error fetching admin dashboard: {}", e.getMessage());
            return ResponseUtil.fail("Failed to fetch admin dashboard", null, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}