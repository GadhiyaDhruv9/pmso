package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import com.pmso.projectManagementSystemOne.repository.TaskRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskController(TaskService taskService, TaskRepository taskRepository) {
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    //CREATE NEW TASK
    @PostMapping("/add-task")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createTask(@PathVariable Long projectId, @RequestBody TaskDto taskDto, Authentication auth) {

        if(taskRepository.existsByTaskName(taskDto.getTaskName()))
            return ResponseUtil.fail("TaskName already exists.", "Conflict", HttpStatus.BAD_REQUEST);

        TaskDto createdTask = taskService.createTask(projectId, taskDto, auth.getName());
        return ResponseUtil.created("Task created successfully", createdTask);
    }

    //GET TASK
    @GetMapping
    public ResponseEntity<?> getTaskByProject(@PathVariable Long projectId) {
        List<TaskDto> tasks = taskService.getTasksByProject(projectId);
        return ResponseUtil.success("Tasks retrieved successfully", tasks);
    }

    //GET TASK BY ID
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable Long projectId, @PathVariable Long taskId) {
        TaskDto tasks = taskService.getTaskById(taskId);
        return ResponseUtil.success("Task retrieved successfully", tasks);
    }

    //UPDATE TASK BY TASK ID
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTask(@PathVariable Long projectId, @PathVariable Long taskId, @RequestBody TaskDto taskDto, Authentication authentication) {
        String username = authentication.getName();
        TaskDto updatedTask = taskService.updateTask(taskId, taskDto, username);
        return ResponseUtil.success("Task updated successfully", updatedTask);
    }

    //DELETE TASK BY ID
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('USER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(@PathVariable Long projectId, @PathVariable Long taskId, Authentication authentication) {
        String username = authentication.getName();
        taskService.deleteTask(taskId, username);
        return ResponseUtil.success("Task deleted successfully", null);
    }

    //ASSIGN TASK TO USER
    @PostMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> assignTaskToUser(@PathVariable Long projectId, @PathVariable Long taskId, @PathVariable Long userId, Authentication authentication) {
        String username = authentication.getName();
        taskService.assignTaskToUser(taskId, userId, username);
        return ResponseUtil.success("Task assigned to user successfully", null);
    }
}