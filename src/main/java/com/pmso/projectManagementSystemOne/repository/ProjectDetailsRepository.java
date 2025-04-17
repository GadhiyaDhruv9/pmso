package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.ProjectDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProjectDetailsRepository extends JpaRepository<ProjectDetails, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectDetails pd WHERE pd.project.projectId = :projectId")
    void deleteByProject_ProjectId(Long projectId);
}
