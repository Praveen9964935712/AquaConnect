package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aquaconnect.backend.service.voice.DeterministicSpeechToTextService;
import com.aquaconnect.backend.service.voice.SpeechInput;
import com.aquaconnect.backend.service.intent.DeterministicIntentDetectionService;

class VoiceAndIntentServicesTest {

    @Test
    void sttMockTranscribesKnownText() {
        var service = new DeterministicSpeechToTextService();
        var result = service.transcribe(new SpeechInput("There is a pipe leaking near my house", "en", null));

        assertThat(result.transcript()).contains("pipe");
        assertThat(result.language()).isEqualTo("en");
        assertThat(result.confidence()).isGreaterThan(0.0);
    }

    @Test
    void intentDetectionRecognizesWaterLeak() {
        var service = new DeterministicIntentDetectionService();
        var result = service.detect("Water pipe leaking outside my house.", "en");

        assertThat(result.intent()).isEqualTo("PIPELINE_LEAK");
        assertThat(result.confidence()).isGreaterThanOrEqualTo(0.8);
        assertThat(result.clarificationRequired()).isFalse();
    }

    @Test
    void lowConfidenceRequiresClarification() {
        var service = new DeterministicIntentDetectionService();
        var result = service.detect("I need some assistance with water issues", "en");

        assertThat(result.intent()).isEqualTo("UNKNOWN");
        assertThat(result.clarificationRequired()).isTrue();
    }

    @Test
    void emptyAudioAndUnsupportedLanguageAreRejected() {
        var stt = new DeterministicSpeechToTextService();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> stt.transcribe(new SpeechInput("   ", "en", null)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> stt.transcribe(new SpeechInput("There is a leak", "fr", null)));

        var intent = new DeterministicIntentDetectionService();
        var result = intent.detect("", "fr");
        assertThat(result.intent()).isEqualTo("UNKNOWN");
        assertThat(result.clarificationRequired()).isTrue();
    }
}
