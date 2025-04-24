package com.pmso.projectManagementSystemOne.mapper;

import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.Task;
import java.util.stream.Collectors;

public class TaskMapper {

    public static TaskDto mapToTaskDto(Task task) {
        TaskDto taskDto = new TaskDto();
        if (task != null) {
            taskDto.setTaskId(task.getTaskId());
            taskDto.setTaskName(task.getTaskName());
            taskDto.setTaskStatus(task.getTaskStatus());
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
            // Map all assigned users from TaskAssignment
            if (task.getTaskAssignments() != null) {
                taskDto.setAssignedToUsername(
                        task.getTaskAssignments().stream()
                                .filter(taskAssignment -> taskAssignment.getUser() != null)
                                .map(taskAssignment -> taskAssignment.getUser().getUsername())
                                .collect(Collectors.toList())
                );
            }
        }
        return taskDto;
    }

    public static Task mapToTask(TaskDto taskDto) {
        Task task = new Task();
        if (taskDto != null) {
            task.setTaskId(taskDto.getTaskId());
            task.setTaskName(taskDto.getTaskName());
            task.setTaskStatus(taskDto.getTaskStatus() != null ? taskDto.getTaskStatus() : "Draft");
            task.setTaskPriority(taskDto.getTaskPriority());
            task.setTaskDescription(taskDto.getTaskDescription());
        }
        return task;
    }
}