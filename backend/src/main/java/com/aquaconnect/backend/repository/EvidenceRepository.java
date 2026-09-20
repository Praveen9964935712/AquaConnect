package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.Evidence;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {

    List<Evidence> findByWorkOrderIdOrderByCreatedAtDesc(UUID workOrderId);
}
