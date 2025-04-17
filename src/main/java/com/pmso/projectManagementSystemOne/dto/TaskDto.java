package com.pmso.projectManagementSystemOne.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long taskId;
    private String taskName;
    private String taskStatus;
    private String taskPriority;
    private String taskDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByUsername;
    private String updatedByUsername;
    private Long projectId;
    private String assignedToUsername;
}