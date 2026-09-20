package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.workorder.WorkOrderAssignmentRequest;
import com.aquaconnect.backend.dto.workorder.WorkOrderCreateRequest;
import com.aquaconnect.backend.dto.workorder.WorkOrderFindingsRequest;
import com.aquaconnect.backend.dto.workorder.WorkOrderResponse;
import com.aquaconnect.backend.dto.workorder.WorkOrderStatusRequest;
import com.aquaconnect.backend.service.WorkOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'FIELD_ENGINEER', 'ADMIN')")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'ADMIN')")
    public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workOrderService.create(request.incidentId()));
    }

    @GetMapping
    public List<WorkOrderResponse> findAll(Authentication authentication) {
        return workOrderService.findAllForActor(authentication);
    }

    @GetMapping("/{id}")
    public WorkOrderResponse findById(@PathVariable UUID id, Authentication authentication) {
        return workOrderService.findForActor(id, authentication);
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'ADMIN')")
    public WorkOrderResponse assign(@PathVariable UUID id, @Valid @RequestBody WorkOrderAssignmentRequest request) {
        return workOrderService.assign(id, request.assignedEngineerId());
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse transition(@PathVariable UUID id,
            @Valid @RequestBody WorkOrderStatusRequest request, Authentication authentication) {
        return workOrderService.transition(id, request.status(), authentication);
    }

    @PostMapping("/{id}/accept")
    public WorkOrderResponse accept(@PathVariable UUID id, Authentication authentication) {
        return workOrderService.accept(id, authentication);
    }

    @PostMapping("/{id}/start")
    public WorkOrderResponse start(@PathVariable UUID id, Authentication authentication) {
        return workOrderService.start(id, authentication);
    }

    @PostMapping("/{id}/complete")
    public WorkOrderResponse complete(@PathVariable UUID id,
            @Valid @RequestBody WorkOrderFindingsRequest findings, Authentication authentication) {
        return workOrderService.complete(id, findings, authentication);
    }
}
