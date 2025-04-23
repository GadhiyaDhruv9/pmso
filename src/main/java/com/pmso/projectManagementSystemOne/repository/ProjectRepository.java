package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Boolean existsByProjectName(String projectName);
}