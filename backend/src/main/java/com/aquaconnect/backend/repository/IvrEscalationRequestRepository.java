package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.IvrEscalationRequest;
import com.aquaconnect.backend.enums.IvrEscalationStatus;

public interface IvrEscalationRequestRepository extends JpaRepository<IvrEscalationRequest, UUID> {
    List<IvrEscalationRequest> findByCallerIdentifierOrderByCreatedAtDesc(String callerIdentifier);
    List<IvrEscalationRequest> findByStatusOrderByCreatedAtAsc(IvrEscalationStatus status);
    boolean existsBySessionId(UUID sessionId);
}
