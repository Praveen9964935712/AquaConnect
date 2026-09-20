package com.aquaconnect.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaconnect.backend.entity.ResolutionConfirmation;

public interface ResolutionConfirmationRepository extends JpaRepository<ResolutionConfirmation, UUID> {
}
