package com.pmso.projectManagementSystemOne.repository;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameOrEmail(String username,String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM UserEntity u WHERE u.username = :email OR u.email = :username")
    boolean existsByUsernameAsEmailOrEmailAsUsername(@Param("username") String username, @Param("email") String email);
    Optional<UserEntity> findByUsername(String username);
}
