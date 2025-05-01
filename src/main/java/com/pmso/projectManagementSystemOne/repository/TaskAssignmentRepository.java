package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByUser_Username(String username);
    boolean existsByTask_TaskIdAndUser_Username(Long taskId, String username);
    void deleteByTask_TaskId(Long taskId);
    List<TaskAssignment> findByTask_TaskId(Long taskId); // Added method
}