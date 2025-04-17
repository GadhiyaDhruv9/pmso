package com.pmso.projectManagementSystemOne.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDto {
    private Long projectId;
    private String projectName;
    private String projectType;
    private String projectStatus;
    private String projectDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByUsername;
    private String updatedByUsername;
    private List<String> assignedToUsernames;
    private List<TaskDto> tasks;
}