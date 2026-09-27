package com.aquaconnect.backend.service.voice;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class DeterministicTextToSpeechService implements TextToSpeechService {

    @Override
    public AudioResponse synthesize(String text, String language, String voice) {
        String normalizedText = text == null ? "" : text.trim();
        if (normalizedText.isBlank()) {
            throw new IllegalArgumentException("Cannot synthesize empty text");
        }
        String normalizedLanguage = language == null || language.isBlank() ? "en" : language.toLowerCase(Locale.ROOT);
        if (!"en".equals(normalizedLanguage) && !"hi".equals(normalizedLanguage) && !"kn".equals(normalizedLanguage)) {
            throw new IllegalArgumentException("Unsupported language");
        }
        return new AudioResponse("mock-audio-" + normalizedLanguage, "text/plain", normalizedLanguage,
                ("voice:" + normalizedText).getBytes(StandardCharsets.UTF_8));
    }
}
