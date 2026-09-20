package com.aquaconnect.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.WaterZone;

public interface WaterZoneRepository extends JpaRepository<WaterZone, UUID> {
}
