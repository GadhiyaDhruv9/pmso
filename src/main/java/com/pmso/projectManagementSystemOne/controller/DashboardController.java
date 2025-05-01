package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.Service.ProjectService;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.exception.ApiResponse;
import com.pmso.projectManagementSystemOne.repository.ProjectAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(Authentication auth) {
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
                    .map(assignment -> projectService.getProjectById(assignment.getProject().getProjectId()))
                    .collect(Collectors.toList());
        }

        Map<String, List<ProjectDto>> projectsByStatus = projects.stream()
                .collect(Collectors.groupingBy(
                        project -> project.getProjectStatus() != null ? project.getProjectStatus() : "Draft"
                ));

        List<Map<String, Object>> projectDetails = projectsByStatus.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> statusData = new HashMap<>();
                    statusData.put("projectStatus", entry.getKey());
                    statusData.put("projectCount", entry.getValue().size());
                    return statusData;
                })
                .collect(Collectors.toList());

        Map<String, Object> projectSummary = new HashMap<>();
        projectSummary.put("totalProjects", projects.size());
        projectSummary.put("projectDetails", projectDetails);

        List<Map<String, Object>> taskDetail = new ArrayList<>();
        int totalTask = 0;

        for (ProjectDto project : projects) {
            Map<String, Object> projectTaskData = new LinkedHashMap<>();
            projectTaskData.put("projectname", project.getProjectName());

            List<TaskDto> projectTasks = isAdmin
                    ? taskService.getTasksByProject(project.getProjectId())
                    : taskService.getTasksByAssignedUser(username).stream()
                    .filter(task -> task.getProjectId().equals(project.getProjectId()))
                    .collect(Collectors.toList());

            int projectTotalTask = projectTasks.size();
            projectTaskData.put("totalTask", projectTotalTask);

            Map<String, List<TaskDto>> tasksByStatus = projectTasks.stream()
                    .collect(Collectors.groupingBy(
                            task -> task.getTaskStatus() != null ? task.getTaskStatus() : "Draft"
                    ));

            List<Map<String, Object>> taskStatusDetails = tasksByStatus.entrySet().stream()
                    .map(taskEntry -> {
                        Map<String, Object> taskStatusInfo = new HashMap<>();
                        taskStatusInfo.put("taskStatus", taskEntry.getKey());
                        taskStatusInfo.put("totalTask", taskEntry.getValue().size());
                        return taskStatusInfo;
                    })
                    .sorted((a, b) -> {
                        String statusA = (String) a.get("taskStatus");
                        String statusB = (String) b.get("taskStatus");
                        List<String> order = List.of("pending", "Draft", "Completed");
                        return Integer.compare(order.indexOf(statusA), order.indexOf(statusB));
                    })
                    .collect(Collectors.toList());

            projectTaskData.put("taskStatusDetails", taskStatusDetails);
            taskDetail.add(projectTaskData);
            totalTask += projectTotalTask;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);
        data.put("role", isAdmin ? "ADMIN" : "USER");
        data.put("projectSummary", projectSummary);
        data.put("totalTask", totalTask);
        data.put("taskDetail", taskDetail);

        return ResponseUtil.success("Dashboard retrieved successfully", data);
    }
}