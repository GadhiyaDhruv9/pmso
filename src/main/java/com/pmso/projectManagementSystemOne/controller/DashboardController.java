package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.Service.ProjectService;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.ProjectAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserRepository userRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    public DashboardController(
            ProjectService projectService,
            TaskService taskService,
            UserRepository userRepository,
            ProjectAssignmentRepository projectAssignmentRepository) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.projectAssignmentRepository = projectAssignmentRepository;
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
            List<ProjectDto> assignedProjects = projectAssignmentRepository.findByUser_UserId(user.getUserId())
                    .stream()
                    .map(assignment -> {
                        try {
                            return projectService.getProjectById(assignment.getProject().getProjectId());
                        } catch (Exception e) {
                            logger.warn("Error fetching project {}", assignment.getProject().getProjectId(), e);
                            return null;
                        }
                    })
                    .filter(project -> project != null)
                    .collect(Collectors.toList());

            // Get tasks assigned to the user
            List<TaskDto> assignedTasks = taskService.getTasksByAssignedUser(username);

            // Get project counts by status
            Map<String, Long> projectStatusCounts = projectService.getProjectCountsByStatus(assignedProjects);

            // Get task counts by status
            Map<String, Long> taskStatusCounts = taskService.getTaskCountByStatus(assignedTasks);

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("projects", assignedProjects);
            dashboardData.put("tasks", assignedTasks);
            dashboardData.put("user", username);
            dashboardData.put("projectStatusCounts", projectStatusCounts);
            dashboardData.put("taskStatusCounts", taskStatusCounts);

            logger.info("User dashboard retrieved successfully for user: {}", username);
            return ResponseUtil.success("User dashboard retrieved successfully", dashboardData);
        } catch (Exception e) {
            logger.error("Error fetching user dashboard for {}: {}", auth.getName(), e);
            return ResponseUtil.fail("Failed to fetch user dashboard: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
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
                        assignmentData.put("projectStatus", assignment.getProject().getProjectStatus() != null ?
                                assignment.getProject().getProjectStatus() : "Draft");
                        assignmentData.put("userId", assignment.getUser().getUserId());
                        assignmentData.put("username", assignment.getUser().getUsername());
                        assignmentData.put("role", assignment.getRole() != null ? assignment.getRole() : "MEMBER");
                        return assignmentData;
                    })
                    .collect(Collectors.toList());

            // Get all tasks
            List<TaskDto> allTasks = allProjects.stream()
                    .flatMap(project -> {
                        try {
                            return taskService.getTasksByProject(project.getProjectId()).stream();
                        } catch (Exception e) {
                            logger.warn("Error fetching tasks for project {}", project.getProjectId(), e);
                            return Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());

            // Get project counts by status
            Map<String, Long> projectStatusCounts = projectService.getAllProjectCountsByStatus();

            // Get task counts by status
            Map<String, Long> taskStatusCounts = taskService.getAllTaskCountsByStatus();

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("projects", allProjects);
            dashboardData.put("projectAssignments", projectAssignments);
            dashboardData.put("tasks", allTasks);
            dashboardData.put("projectStatusCounts", projectStatusCounts);
            dashboardData.put("taskStatusCounts", taskStatusCounts);

            logger.info("Admin dashboard retrieved successfully");
            return ResponseUtil.success("Admin dashboard retrieved successfully", dashboardData);
        } catch (Exception e) {
            logger.error("Error fetching admin dashboard", e);
            return ResponseUtil.fail("Failed to fetch admin dashboard: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}