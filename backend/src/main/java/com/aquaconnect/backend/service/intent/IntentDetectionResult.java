package com.aquaconnect.backend.service.intent;

public record IntentDetectionResult(
        String intent,
        double confidence,
        String description,
        String language,
        boolean clarificationRequired) {}
