package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aquaconnect.backend.entity.Incident;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    List<Incident> findByCitizenIdOrderByCreatedAtDesc(UUID citizenId);

    Optional<Incident> findByIdAndCitizenId(UUID id, UUID citizenId);
    List<Incident> findByCallerIdentifierOrderByCreatedAtDesc(String callerIdentifier);

    long countByStatus(com.aquaconnect.backend.enums.IncidentStatus status);
    long countByPriority(com.aquaconnect.backend.enums.IncidentPriority priority);
    long countByCategory(com.aquaconnect.backend.enums.IncidentCategory category);
    long countBySource(com.aquaconnect.backend.enums.IncidentSource source);

    @Query("select count(i) from Incident i where i.status not in :closedStatuses")
    long countOpen(@Param("closedStatuses") java.util.Collection<com.aquaconnect.backend.enums.IncidentStatus> closedStatuses);
}
