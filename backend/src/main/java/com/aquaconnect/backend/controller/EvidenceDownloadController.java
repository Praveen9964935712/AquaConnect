package com.aquaconnect.backend.controller;

import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.service.EvidenceService;

@RestController
@RequestMapping("/api/evidence")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'FIELD_ENGINEER', 'ADMIN')")
public class EvidenceDownloadController {

    private final EvidenceService evidenceService;

    public EvidenceDownloadController(EvidenceService evidenceService) { this.evidenceService = evidenceService; }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable UUID id, Authentication authentication) {
        EvidenceService.EvidenceDownload download = evidenceService.download(id, authentication);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.evidence().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + download.evidence().getOriginalFilename() + "\"")
                .contentLength(download.bytes().length).body(new ByteArrayResource(download.bytes()));
    }
}