package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.WorkOrder;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    Optional<WorkOrder> findByIncidentId(UUID incidentId);

    List<WorkOrder> findByAssignedEngineerIdOrderByCreatedAtDesc(UUID engineerId);

    List<WorkOrder> findAllByOrderByCreatedAtDesc();
}
