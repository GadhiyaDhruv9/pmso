package com.pmso.projectManagementSystemOne.repository;

import com.pmso.projectManagementSystemOne.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Query("DELETE FROM RefreshToken r WHERE r.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}