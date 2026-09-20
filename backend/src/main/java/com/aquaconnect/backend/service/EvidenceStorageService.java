package com.aquaconnect.backend.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface EvidenceStorageService {

    void store(String objectKey, MultipartFile file) throws IOException;

    byte[] read(String objectKey) throws IOException;
}
