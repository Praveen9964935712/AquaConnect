package com.aquaconnect.backend.dto.admin;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String email,
        boolean active,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt) {
}
