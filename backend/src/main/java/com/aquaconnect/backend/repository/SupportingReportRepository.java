package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.SupportingReport;

public interface SupportingReportRepository extends JpaRepository<SupportingReport, UUID> {

    boolean existsByPrimaryIncidentIdAndSupportingIncidentId(UUID primaryIncidentId, UUID supportingIncidentId);

    List<SupportingReport> findByPrimaryIncidentId(UUID primaryIncidentId);

    long countByPrimaryIncidentId(UUID primaryIncidentId);
}
