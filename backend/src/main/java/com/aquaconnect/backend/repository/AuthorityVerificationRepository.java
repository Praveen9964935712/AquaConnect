package com.aquaconnect.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.AuthorityVerification;
import com.aquaconnect.backend.enums.VerificationDecision;

public interface AuthorityVerificationRepository extends JpaRepository<AuthorityVerification, UUID> {
    long countByDecision(VerificationDecision decision);
}
