package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.admin.AdminAssetResponse;
import com.aquaconnect.backend.dto.admin.AdminRoleResponse;
import com.aquaconnect.backend.dto.admin.AdminUserResponse;
import com.aquaconnect.backend.dto.admin.AdminZoneResponse;
import com.aquaconnect.backend.dto.admin.RoleAssignmentRequest;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.InfrastructureAssetRepository;
import com.aquaconnect.backend.repository.RoleRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WaterZoneRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Transactional(readOnly = true)
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WaterZoneRepository waterZoneRepository;
    private final InfrastructureAssetRepository infrastructureAssetRepository;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository,
            WaterZoneRepository waterZoneRepository, InfrastructureAssetRepository infrastructureAssetRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.waterZoneRepository = waterZoneRepository;
        this.infrastructureAssetRepository = infrastructureAssetRepository;
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.isActive(),
                        user.getUserRoles().stream().map(userRole -> userRole.getRole().getName().name()).sorted().toList(),
                        user.getCreatedAt(), user.getUpdatedAt()))
                .toList();
    }

    @GetMapping("/roles")
    public List<AdminRoleResponse> roles() {
        return roleRepository.findAll().stream()
                .map(role -> new AdminRoleResponse(role.getId(), role.getName().name(), role.getDescription()))
                .toList();
    }

    @GetMapping("/zones")
    public List<AdminZoneResponse> zones() {
        return waterZoneRepository.findAll().stream().map(AdminZoneResponse::from).toList();
    }

    @GetMapping("/infrastructure/assets")
    public List<AdminAssetResponse> infrastructureAssets() {
        return infrastructureAssetRepository.findAll().stream().map(AdminAssetResponse::from).toList();
    }

    @PostMapping("/users/{userId}/roles")
    @Transactional
    public AdminUserResponse assignRole(@PathVariable UUID userId, @Valid @RequestBody RoleAssignmentRequest request) {
        if (request.role() == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (request.role() == RoleName.CITIZEN) {
            throw new IllegalArgumentException("Citizen role cannot be assigned here");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(request.role())
                .orElseGet(() -> roleRepository.saveAndFlush(new Role(request.role(), request.role().name())));

        boolean alreadyAssigned = user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName() == request.role());
        if (!alreadyAssigned) {
            user.getUserRoles().add(new UserRole(user, role));
            userRepository.save(user);
        }

        return new AdminUserResponse(user.getId(), user.getUsername(), user.getEmail(), user.isActive(),
                user.getUserRoles().stream().map(userRole -> userRole.getRole().getName().name()).sorted().toList(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
