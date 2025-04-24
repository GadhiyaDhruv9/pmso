package com.pmso.projectManagementSystemOne.Service.impl;

import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.*;
import com.pmso.projectManagementSystemOne.mapper.TaskMapper;
import com.pmso.projectManagementSystemOne.repository.*;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Override
    @Transactional
    public TaskDto createTask(Long projectId, TaskDto taskDto, String username) {
        logger.info("Creating task for project {} by user: {}", projectId, username);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = TaskMapper.mapToTask(taskDto);
        if (task.getTaskStatus() == null) {
            task.setTaskStatus("Draft");
        }
        task.setProject(project);
        task.setAssignedTo(user);
        task.setCreatedBy(user);
        task.setUpdatedBy(user);
        Task savedTask = taskRepository.save(task);

        logger.info("Task {} created successfully", savedTask.getTaskId());
        return TaskMapper.mapToTaskDto(savedTask);
    }

    @Override
    public List<TaskDto> getTasksByAssignedUser(String username) {
        try {
            List<Task> tasks = taskRepository.findByAssignedTo_Username(username);
            return tasks.stream()
                    .map(TaskMapper::mapToTaskDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching tasks for user {}", username, e);
            throw new RuntimeException("Failed to fetch tasks for user: " + username, e);
        }
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long taskId, TaskDto taskDto, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UserEntity updater = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setTaskName(taskDto.getTaskName());
        task.setTaskStatus(taskDto.getTaskStatus() != null ? taskDto.getTaskStatus() : "Draft");
        task.setTaskPriority(taskDto.getTaskPriority());
        task.setTaskDescription(taskDto.getTaskDescription());
        task.setUpdatedBy(updater);
        Task updatedTask = taskRepository.save(task);

        return TaskMapper.mapToTaskDto(updatedTask);
    }

    @Override
    public List<TaskDto> getTasksByProject(Long projectId) {
        return taskRepository.findByProject_ProjectId(projectId).stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    @Override
    public TaskDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return TaskMapper.mapToTaskDto(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, String username) {
        logger.info("Attempting to delete task {} by user: {}", taskId, username);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Delete all associated task assignments
        taskAssignmentRepository.deleteByTask_TaskId(taskId);

        taskRepository.delete(task);
        logger.info("Task {} deleted successfully", taskId);
    }

    @Override
    @Transactional
    public void assignTaskToUser(Long taskId, Long userId, String username) {
        logger.info("Assigning task {} to user {} by user: {}", taskId, userId, username);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserEntity assigner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Assigner not found"));

        Project project = task.getProject();
        List<ProjectAssignment> assignments = projectAssignmentRepository.findByProject_ProjectId(project.getProjectId());
        boolean isUserAssignedToProject = assignments.stream()
                .anyMatch(assignment -> assignment.getUser().getUserId().equals(userId));

        if (!isUserAssignedToProject) {
            throw new RuntimeException("User is not assigned to the project");
        }

        TaskAssignment taskAssignment = new TaskAssignment(task, user);
        taskAssignment.setCreatedBy(assigner);
        taskAssignment.setUpdatedBy(assigner);
        taskAssignmentRepository.save(taskAssignment);

        task.setAssignedTo(user);
        task.setUpdatedBy(assigner);
        taskRepository.save(task);

        logger.info("Task {} assigned to user {} successfully", taskId, userId);
    }

    @Override
    public Map<String, Long> getTaskCountByStatus(List<TaskDto> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getTaskStatus() != null ? task.getTaskStatus() : "Draft",
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getAllTaskCountsByStatus() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getTaskStatus() != null ? task.getTaskStatus() : "Draft",
                        Collectors.counting()
                ));
    }
}