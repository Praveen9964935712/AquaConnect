package com.aquaconnect.backend.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.SupportingReport;
import com.aquaconnect.backend.exception.InvalidSupportingReportException;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.SupportingReportRepository;

@Service
public class SupportingReportService {

    private final IncidentRepository incidentRepository;
    private final SupportingReportRepository supportingReportRepository;

    public SupportingReportService(IncidentRepository incidentRepository,
            SupportingReportRepository supportingReportRepository) {
        this.incidentRepository = incidentRepository;
        this.supportingReportRepository = supportingReportRepository;
    }

    @Transactional
    public SupportingReport addForCitizen(UUID citizenId, UUID primaryIncidentId, UUID supportingIncidentId) {
        if (primaryIncidentId.equals(supportingIncidentId)) {
            throw new InvalidSupportingReportException("An incident cannot support itself");
        }
        Incident primary = incidentRepository.findById(primaryIncidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Primary incident not found"));
        Incident supporting = incidentRepository.findByIdAndCitizenId(supportingIncidentId, citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Supporting incident not found"));
        if (supportingReportRepository.existsByPrimaryIncidentIdAndSupportingIncidentId(primaryIncidentId, supportingIncidentId)) {
            throw new InvalidSupportingReportException("Supporting relationship already exists");
        }
        if (supportingReportRepository.existsByPrimaryIncidentIdAndSupportingIncidentId(supportingIncidentId, primaryIncidentId)) {
            throw new InvalidSupportingReportException("Circular supporting relationship is not allowed");
        }
        try {
            return supportingReportRepository.saveAndFlush(new SupportingReport(primary, supporting));
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidSupportingReportException("Supporting relationship is invalid");
        }
    }
}
