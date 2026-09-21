package com.aquaconnect.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.IvrSession;

public interface IvrSessionRepository extends JpaRepository<IvrSession, UUID> {
    List<IvrSession> findByCallerIdentifierOrderByCreatedAtDesc(String callerIdentifier);
}
