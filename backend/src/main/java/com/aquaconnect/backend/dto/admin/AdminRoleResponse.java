package com.aquaconnect.backend.dto.admin;

import java.util.UUID;

public record AdminRoleResponse(UUID id, String name, String description) {
}
