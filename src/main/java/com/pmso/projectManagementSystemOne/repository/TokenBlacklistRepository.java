package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);
    boolean existsByToken(String token);
}