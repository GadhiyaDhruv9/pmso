package com.pmso.projectManagementSystemOne.Service;

import com.pmso.projectManagementSystemOne.dto.TaskDto;

import java.util.List;
import java.util.Map;

public interface TaskService {

    // CREATE TASK FOR PROJECT WITH PROJECT ID
    TaskDto createTask(Long projectId, TaskDto taskDto, String username);

    // GET TASK BY PROJECT ID
    List<TaskDto> getTasksByProject(Long projectId);

    // GET TASK BY TASK ID
    TaskDto getTaskById(Long taskId);

    // UPDATE TASK
    TaskDto updateTask(Long taskId, TaskDto taskDto, String username);

    // DELETE TASK
    void deleteTask(Long taskId, String username);

    // ASSIGN TASK TO USER
    void assignTaskToUser(Long taskId, Long userId, String username);

    List<TaskDto> getTasksByAssignedUser(String username);

    Map<String, Long> getTaskCountByStatus(List<TaskDto> tasks);
    Map<String, Long> getAllTaskCountsByStatus();
}