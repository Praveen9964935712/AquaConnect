package com.aquaconnect.backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.admin.AdminZoneResponse;
import com.aquaconnect.backend.dto.admin.InfrastructureAssetResponse;
import com.aquaconnect.backend.service.InfrastructureService;

@RestController
@RequestMapping("/api/infrastructure")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'FIELD_ENGINEER', 'ADMIN')")
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    public InfrastructureController(InfrastructureService infrastructureService) {
        this.infrastructureService = infrastructureService;
    }

    @GetMapping("/assets")
    public List<InfrastructureAssetResponse> assets() {
        return infrastructureService.findAllActiveAssets().stream().map(InfrastructureAssetResponse::from).toList();
    }

    @GetMapping("/zones")
    public List<AdminZoneResponse> zones() {
        return infrastructureService.findAllZones().stream().map(AdminZoneResponse::from).toList();
    }
}
