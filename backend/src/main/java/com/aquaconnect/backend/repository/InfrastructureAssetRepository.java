package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.InfrastructureAsset;

public interface InfrastructureAssetRepository extends JpaRepository<InfrastructureAsset, UUID> {

    List<InfrastructureAsset> findByActiveTrueOrderByNameAsc();
}
