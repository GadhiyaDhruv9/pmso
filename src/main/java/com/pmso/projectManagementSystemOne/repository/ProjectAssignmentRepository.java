package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
    List<ProjectAssignment> findByProject_ProjectId(Long projectId);
    List<ProjectAssignment> findByUser_UserId(Long userId); // Added method
}