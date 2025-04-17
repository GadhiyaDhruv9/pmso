package com.pmso.projectManagementSystemOne.mapper;

import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.Task;
import com.pmso.projectManagementSystemOne.enums.Status;

public class TaskMapper {

    public static TaskDto mapToTaskDto(Task task) {
        TaskDto taskDto = new TaskDto();
        if (task != null) {
            taskDto.setTaskId(task.getTaskId());
            taskDto.setTaskName(task.getTaskName());
            taskDto.setTaskStatus(task.getTaskStatus().name()); // Convert enum to string
            taskDto.setTaskPriority(task.getTaskPriority());
            taskDto.setTaskDescription(task.getTaskDescription());
            taskDto.setCreatedAt(task.getCreatedAt());
            taskDto.setUpdatedAt(task.getUpdatedAt());
            if (task.getCreatedBy() != null) {
                taskDto.setCreatedByUsername(task.getCreatedBy().getUsername());
            }
            if (task.getUpdatedBy() != null) {
                taskDto.setUpdatedByUsername(task.getUpdatedBy().getUsername());
            }
            if (task.getProject() != null) {
                taskDto.setProjectId(task.getProject().getProjectId());
            }
            if (task.getAssignedTo() != null) {
                taskDto.setAssignedToUsername(task.getAssignedTo().getUsername());
            }
        }
        return taskDto;
    }

    public static Task mapToTask(TaskDto taskDto) {
        Task task = new Task();
        if (taskDto != null) {
            task.setTaskId(taskDto.getTaskId());
            task.setTaskName(taskDto.getTaskName());
            // Handle status conversion with null check and default value
            if (taskDto.getTaskStatus() != null) {
                String statusValue = taskDto.getTaskStatus().replace(" ", "_");
                task.setTaskStatus(Status.valueOf(statusValue));
            } else {
                task.setTaskStatus(Status.Draft); // Default value
            }
            task.setTaskPriority(taskDto.getTaskPriority());
            task.setTaskDescription(taskDto.getTaskDescription());
        }
        return task;
    }
}