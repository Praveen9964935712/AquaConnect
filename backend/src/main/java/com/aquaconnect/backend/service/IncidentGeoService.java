package com.aquaconnect.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aquaconnect.backend.entity.IncidentGeoPoint;
import com.aquaconnect.backend.repository.IncidentGeoRepository;

@Service
public class IncidentGeoService {

    private final IncidentGeoRepository incidentGeoRepository;

    public IncidentGeoService(IncidentGeoRepository incidentGeoRepository) {
        this.incidentGeoRepository = incidentGeoRepository;
    }

    public List<IncidentGeoPoint> findNearby(BigDecimal latitude, BigDecimal longitude, double radiusMeters) {
        return incidentGeoRepository.findNearby(latitude, longitude, radiusMeters);
    }

    public List<IncidentGeoPoint> findNearby(BigDecimal latitude, BigDecimal longitude) {
        return findNearby(latitude, longitude, 1_000.0);
    }
}
