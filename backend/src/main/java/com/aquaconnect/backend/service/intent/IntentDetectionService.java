package com.aquaconnect.backend.service.intent;

public interface IntentDetectionService {
    IntentDetectionResult detect(String text, String language);
}
