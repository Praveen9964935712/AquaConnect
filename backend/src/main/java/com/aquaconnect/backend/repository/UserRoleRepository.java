package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    Optional<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    List<UserRole> findByUserId(UUID userId);

    List<UserRole> findByRoleId(UUID roleId);
}