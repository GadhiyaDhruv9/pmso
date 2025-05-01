package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.DocumentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentMasterRepository extends JpaRepository<DocumentMaster, Long> {
    Optional<DocumentMaster> findByDocumentCode(String documentCode);
}