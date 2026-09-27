package com.aquaconnect.backend.service.voice;

public interface TextToSpeechService {
    AudioResponse synthesize(String text, String language, String voice);
}
