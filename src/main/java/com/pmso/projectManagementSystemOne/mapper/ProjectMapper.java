package com.pmso.projectManagementSystemOne.mapper;

import com.pmso.projectManagementSystemOne.dto.ProjectDto;
import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.Project;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static ProjectDto mapToProjectDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(project.getProjectId());
        dto.setProjectName(project.getProjectName());
        dto.setProjectType(project.getProjectType());
        dto.setProjectStatus(project.getProjectStatus());
        dto.setProjectDescription(project.getProjectDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        if (project.getCreatedBy() != null) {
            dto.setCreatedByUsername(project.getCreatedBy().getUsername());
        }
        if (project.getUpdatedBy() != null) {
            dto.setUpdatedByUsername(project.getUpdatedBy().getUsername());
        }

        if (project.getAssignments() != null) {
            List<String> assignedUsernames = project.getAssignments().stream()
                    .map(assignment -> assignment.getUser().getUsername())
                    .collect(Collectors.toList());
            dto.setAssignedToUsernames(assignedUsernames);
        }

        if (project.getTasks() != null) {
            List<TaskDto> taskDtos = project.getTasks().stream()
                    .map(TaskMapper::mapToTaskDto)
                    .collect(Collectors.toList());
            dto.setTasks(taskDtos);
        }

        return dto;
    }

    public static Project mapToProject(ProjectDto dto) {
        Project project = new Project();
        project.setProjectId(dto.getProjectId());
        project.setProjectName(dto.getProjectName());
        project.setProjectType(dto.getProjectType());
        project.setProjectStatus(dto.getProjectStatus() != null ? dto.getProjectStatus() : "Draft");
        project.setProjectDescription(dto.getProjectDescription());
        return project;
    }
}