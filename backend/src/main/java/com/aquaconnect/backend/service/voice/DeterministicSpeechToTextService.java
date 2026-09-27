package com.aquaconnect.backend.service.voice;

import java.util.Locale;

public class DeterministicSpeechToTextService implements SpeechToTextService {

    @Override
    public SpeechRecognitionResult transcribe(SpeechInput input) {
        String text = input.audioText() == null ? "" : input.audioText().trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException("Empty audio input");
        }
        String language = input.language() == null || input.language().isBlank() ? "en" : input.language().toLowerCase(Locale.ROOT);
        if (!"en".equals(language) && !"hi".equals(language) && !"kn".equals(language)) {
            throw new IllegalArgumentException("Unsupported language");
        }
        double confidence = input.confidence() == null ? 0.92 : Math.max(0.0, Math.min(1.0, input.confidence()));
        return new SpeechRecognitionResult(text, language, confidence);
    }
}
