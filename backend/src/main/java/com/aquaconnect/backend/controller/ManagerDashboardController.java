package com.aquaconnect.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.dashboard.ManagerDashboardResponse;
import com.aquaconnect.backend.service.OperationalDashboardService;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasAnyRole('OPERATIONS_MANAGER', 'ADMIN')")
public class ManagerDashboardController {

    private final OperationalDashboardService dashboardService;

    public ManagerDashboardController(OperationalDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ManagerDashboardResponse dashboard() {
        return dashboardService.managerDashboard();
    }
}
