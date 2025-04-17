package com.pmso.projectManagementSystemOne.Service;

import com.pmso.projectManagementSystemOne.dto.ProjectDto;

import java.util.List;

public interface ProjectService {

    // CREATE NEW PROJECT
    ProjectDto createProject(ProjectDto projectDto, String username);

    // GET PROJECT BY PROJECT ID
    ProjectDto getProjectById(Long id);

    // GET ALL PROJECTS
    List<ProjectDto> getAllProjects();

    // UPDATE PROJECT
    ProjectDto updateProject(Long id, ProjectDto projectDto, String username);

    // DELETE PROJECT BY PROJECT ID
    void deleteProject(Long projectId, String username);

    // ASSIGN PROJECT TO USER
    void assignUserToProject(Long projectId, Long userId, String role);
}