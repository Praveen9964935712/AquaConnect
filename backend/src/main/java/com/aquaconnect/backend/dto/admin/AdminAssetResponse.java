package com.aquaconnect.backend.dto.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.InfrastructureAsset;

public record AdminAssetResponse(UUID id, String assetType, String name, String identifier, String description,
        BigDecimal latitude, BigDecimal longitude, UUID zoneId, boolean active, Instant createdAt, Instant updatedAt) {
    public static AdminAssetResponse from(InfrastructureAsset asset) {
        return new AdminAssetResponse(asset.getId(), asset.getAssetType().name(), asset.getName(), asset.getIdentifier(),
                asset.getDescription(), asset.getLatitude(), asset.getLongitude(), asset.getZone() == null ? null : asset.getZone().getId(),
                asset.isActive(), asset.getCreatedAt(), asset.getUpdatedAt());
    }
}
