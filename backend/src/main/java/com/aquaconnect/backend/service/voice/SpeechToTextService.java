package com.aquaconnect.backend.service.voice;

public interface SpeechToTextService {
    SpeechRecognitionResult transcribe(SpeechInput input);
}
