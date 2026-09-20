package com.aquaconnect.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aquaconnect.backend.dto.evidence.EvidenceResponse;
import com.aquaconnect.backend.entity.Evidence;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.EvidenceType;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.exception.InvalidEvidenceException;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.EvidenceRepository;
import com.aquaconnect.backend.repository.UserRepository;

@Service
public class EvidenceService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of("image/jpeg", "image/png", "application/pdf");

    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final WorkOrderService workOrderService;
    private final EvidenceStorageService storageService;

    public EvidenceService(EvidenceRepository evidenceRepository, UserRepository userRepository,
            WorkOrderService workOrderService, EvidenceStorageService storageService) {
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
        this.workOrderService = workOrderService;
        this.storageService = storageService;
    }

    @Transactional
    public EvidenceResponse upload(UUID workOrderId, EvidenceType type, MultipartFile file, Authentication authentication) {
        if (type == null || file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE
                || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidEvidenceException("Evidence type, size, and content type are invalid");
        }
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FIELD_ENGINEER"))) {
            throw new InvalidEvidenceException("Only field engineers can upload evidence");
        }
        WorkOrder workOrder = workOrderService.getAuthorizedWorkOrder(workOrderId, authentication);
        User uploader = userRepository.findById(UUID.fromString(authentication.getName()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String originalFilename = safeFilename(file.getOriginalFilename());
        String extension = extension(file.getContentType());
        String objectKey = "work-orders/" + workOrderId + "/" + UUID.randomUUID() + extension;
        try {
            storageService.store(objectKey, file);
        } catch (IOException exception) {
            throw new InvalidEvidenceException("Evidence storage is unavailable");
        }
        return EvidenceResponse.from(evidenceRepository.saveAndFlush(
                new Evidence(workOrder, uploader, type, objectKey, originalFilename, file.getContentType(), file.getSize())));
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> list(UUID workOrderId, Authentication authentication) {
        workOrderService.getAuthorizedWorkOrder(workOrderId, authentication);
        return evidenceRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId).stream()
                .map(EvidenceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EvidenceDownload download(UUID evidenceId, Authentication authentication) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found"));
        workOrderService.getAuthorizedWorkOrder(evidence.getWorkOrder().getId(), authentication);
        try {
            return new EvidenceDownload(evidence, storageService.read(evidence.getObjectKey()));
        } catch (IOException exception) {
            throw new InvalidEvidenceException("Evidence storage is unavailable");
        }
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidEvidenceException("Filename is invalid");
        }
        return filename.length() > 255 ? filename.substring(0, 255) : filename;
    }

    private String extension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> throw new InvalidEvidenceException("Content type is invalid");
        };
    }

    public record EvidenceDownload(Evidence evidence, byte[] bytes) { }
}