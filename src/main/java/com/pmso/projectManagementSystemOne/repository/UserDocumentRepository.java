package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    List<UserDocument> findByUserId(Long userId);

    boolean existsByUserIdAndDocumentMaster_DocumentCode(Long userId, String documentCode);
}