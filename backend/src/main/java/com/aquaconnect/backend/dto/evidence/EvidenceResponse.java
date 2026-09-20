package com.aquaconnect.backend.dto.evidence;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.Evidence;
import com.aquaconnect.backend.enums.EvidenceType;

public record EvidenceResponse(UUID id, UUID workOrderId, EvidenceType evidenceType,
        String originalFilename, String contentType, long fileSize, Instant createdAt) {

    public static EvidenceResponse from(Evidence evidence) {
        return new EvidenceResponse(evidence.getId(), evidence.getWorkOrder().getId(), evidence.getEvidenceType(),
                evidence.getOriginalFilename(), evidence.getContentType(), evidence.getFileSize(), evidence.getCreatedAt());
    }
}