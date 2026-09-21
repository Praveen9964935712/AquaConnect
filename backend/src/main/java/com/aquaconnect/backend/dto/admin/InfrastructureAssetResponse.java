package com.aquaconnect.backend.dto.admin;

import java.math.BigDecimal;
import java.util.UUID;

import com.aquaconnect.backend.entity.InfrastructureAsset;

public record InfrastructureAssetResponse(UUID id, String assetType, String name, String identifier,
        BigDecimal latitude, BigDecimal longitude, UUID zoneId) {
    public static InfrastructureAssetResponse from(InfrastructureAsset asset) {
        return new InfrastructureAssetResponse(asset.getId(), asset.getAssetType().name(), asset.getName(), asset.getIdentifier(),
                asset.getLatitude(), asset.getLongitude(), asset.getZone() == null ? null : asset.getZone().getId());
    }
}
