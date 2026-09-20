package com.aquaconnect.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.InfrastructureAssetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "infrastructure_assets")
public class InfrastructureAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 32)
    private InfrastructureAssetType assetType;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "identifier", nullable = false, length = 100)
    private String identifier;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "geom")
    private String geom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", foreignKey = @ForeignKey(name = "fk_infrastructure_assets_zone"))
    private WaterZone zone;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected InfrastructureAsset() {}

    public InfrastructureAsset(InfrastructureAssetType assetType, String name, String identifier,
            String description, BigDecimal latitude, BigDecimal longitude, WaterZone zone, boolean active) {
        this.assetType = assetType;
        this.name = name;
        this.identifier = identifier;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.zone = zone;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public InfrastructureAssetType getAssetType() { return assetType; }
    public String getName() { return name; }
    public String getIdentifier() { return identifier; }
    public String getDescription() { return description; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public String getGeom() { return geom; }
    public WaterZone getZone() { return zone; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
