package com.aquaconnect.backend.dto.admin;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.WaterZone;

public record AdminZoneResponse(UUID id, String code, String name, String description, boolean active, Instant createdAt, Instant updatedAt) {
    public static AdminZoneResponse from(WaterZone zone) {
        return new AdminZoneResponse(zone.getId(), zone.getCode(), zone.getName(), zone.getDescription(), zone.isActive(), zone.getCreatedAt(), zone.getUpdatedAt());
    }
}
