package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.Incident;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findByCitizenIdOrderByCreatedAtDesc(UUID citizenId);

    Optional<Incident> findByIdAndCitizenId(UUID id, UUID citizenId);
}
