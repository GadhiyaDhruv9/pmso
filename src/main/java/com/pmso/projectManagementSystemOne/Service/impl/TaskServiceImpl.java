package com.pmso.projectManagementSystemOne.Service.impl;

import com.pmso.projectManagementSystemOne.dto.TaskDto;
import com.pmso.projectManagementSystemOne.entity.Project;
import com.pmso.projectManagementSystemOne.entity.ProjectAssignment;
import com.pmso.projectManagementSystemOne.entity.Task;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.mapper.TaskMapper;
import com.pmso.projectManagementSystemOne.repository.ProjectAssignmentRepository;
import com.pmso.projectManagementSystemOne.repository.ProjectRepository;
import com.pmso.projectManagementSystemOne.repository.TaskRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.Service.TaskService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.pmso.projectManagementSystemOne.exception.InvalidStatusTransitionException;
import com.pmso.projectManagementSystemOne.enums.Status;

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

    @Override
    @Transactional
    public TaskDto createTask(Long projectId, TaskDto taskDto, String username) {
        logger.info("Creating task for project {} by user: {}", projectId, username);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = TaskMapper.mapToTask(taskDto);
        // Ensure status is set to Draft if not provided
        if (task.getTaskStatus() == null) {
            task.setTaskStatus(Status.Draft);
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
    @Transactional
    public TaskDto updateTask(Long taskId, TaskDto taskDto, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UserEntity updater = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate status transition
        Status currentStatus = task.getTaskStatus();
        Status newStatus = taskDto.getTaskStatus() != null ?
                Status.valueOf(taskDto.getTaskStatus().replace(" ", "_")) :
                Status.Draft;

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition task status from %s to %s. " +
                                    "Valid transitions are: Draft → Pending → In_Progress → Completed",
                            currentStatus, newStatus));
        }

        task.setTaskName(taskDto.getTaskName());
        task.setTaskStatus(newStatus);
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

        task.setAssignedTo(user);
        task.setUpdatedBy(assigner);
        taskRepository.save(task);

        logger.info("Task {} assigned to user {} successfully", taskId, userId);
    }
}