package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject_ProjectId(Long projectId);
    List<Task> findByAssignedTo_Username(String username);
    Boolean existsByTaskName(String taskName);
}