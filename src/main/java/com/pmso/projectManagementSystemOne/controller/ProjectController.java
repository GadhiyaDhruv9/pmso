package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.Service.ProjectService;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private static final Logger logger = LoggerFactory.getLogger(ProjectDto.class);

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/add-project")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createProject(@RequestBody ProjectDto projectDto, Authentication auth) {
        ProjectDto created = projectService.createProject(projectDto, auth.getName());
        return ResponseUtil.created("Project created successfully", created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseUtil.success("Project retrieved successfully", project);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDto dto, Authentication auth) {
        ProjectDto updated = projectService.updateProject(id, dto, auth.getName());
        return ResponseUtil.success("Project updated successfully", updated);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long projectId, Authentication auth) {
        logger.info("Attempting to delete project with ID: {}", projectId);
        projectService.deleteProject(projectId, auth.getName());
        return ResponseUtil.success("Project deleted successfully", null);
    }

    @PostMapping("/{projectId}/assign/{userId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> assignUserToProject(@PathVariable Long projectId, @PathVariable Long userId, @RequestParam String role) {
        projectService.assignUserToProject(projectId, userId, role);
        return ResponseUtil.success("User assigned to project successfully", null);
    }
}