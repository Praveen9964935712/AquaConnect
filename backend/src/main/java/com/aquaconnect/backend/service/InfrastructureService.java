package com.aquaconnect.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.entity.InfrastructureAsset;
import com.aquaconnect.backend.entity.WaterZone;
import com.aquaconnect.backend.repository.InfrastructureAssetRepository;
import com.aquaconnect.backend.repository.WaterZoneRepository;

@Service
public class InfrastructureService {

    private final InfrastructureAssetRepository infrastructureAssetRepository;
    private final WaterZoneRepository waterZoneRepository;

    public InfrastructureService(InfrastructureAssetRepository infrastructureAssetRepository,
            WaterZoneRepository waterZoneRepository) {
        this.infrastructureAssetRepository = infrastructureAssetRepository;
        this.waterZoneRepository = waterZoneRepository;
    }

    @Transactional(readOnly = true)
    public List<InfrastructureAsset> findAllActiveAssets() {
        return infrastructureAssetRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<WaterZone> findAllZones() {
        return waterZoneRepository.findAll();
    }
}
