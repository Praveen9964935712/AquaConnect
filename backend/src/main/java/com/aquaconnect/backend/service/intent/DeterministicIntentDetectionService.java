package com.aquaconnect.backend.service.intent;

import java.util.Locale;

public class DeterministicIntentDetectionService implements IntentDetectionService {

    @Override
    public IntentDetectionResult detect(String text, String language) {
        String normalized = text == null ? "" : text.trim();
        if (normalized.isBlank()) {
            return new IntentDetectionResult("UNKNOWN", 0.0, "No usable input detected", language == null ? "en" : language, true);
        }
        String lang = language == null || language.isBlank() ? "en" : language.toLowerCase(Locale.ROOT);
        String lower = normalized.toLowerCase(Locale.ROOT);

        if (lower.contains("pipe") && (lower.contains("leak") || lower.contains("burst") || lower.contains("water leaking"))) {
            return new IntentDetectionResult("PIPELINE_LEAK", 0.91, normalized, lang, false);
        }
        if (lower.contains("no water") || lower.contains("water supply") || lower.contains("no supply")) {
            return new IntentDetectionResult("NO_WATER_SUPPLY", 0.89, normalized, lang, false);
        }
        if (lower.contains("low pressure") || lower.contains("pressure")) {
            return new IntentDetectionResult("LOW_WATER_PRESSURE", 0.86, normalized, lang, false);
        }
        if (lower.contains("contaminated") || lower.contains("dirty water") || lower.contains("unsafe water")) {
            return new IntentDetectionResult("CONTAMINATED_WATER", 0.88, normalized, lang, false);
        }
        if (lower.contains("complaint") || lower.contains("status") || lower.contains("existing complaint")) {
            return new IntentDetectionResult("CHECK_COMPLAINT", 0.82, normalized, lang, false);
        }
        if (lower.contains("operator") || lower.contains("speak to operator") || lower.contains("human")) {
            return new IntentDetectionResult("OPERATOR_REQUEST", 0.81, normalized, lang, false);
        }
        if (lower.contains("water supply information") || lower.contains("water supply") || lower.contains("supply information")) {
            return new IntentDetectionResult("WATER_SUPPLY_INFORMATION", 0.84, normalized, lang, false);
        }
        if (!"en".equals(lang) && !"hi".equals(lang) && !"kn".equals(lang)) {
            return new IntentDetectionResult("UNKNOWN", 0.2, "Unsupported language for intent detection", lang, true);
        }
        return new IntentDetectionResult("UNKNOWN", 0.42, "I see a water issue, but I need more information. Is this a leak, no water supply, or low water pressure?", lang, true);
    }
}
