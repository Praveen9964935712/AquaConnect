package com.aquaconnect.backend.service.voice;

public record SpeechRecognitionResult(String transcript, String language, double confidence) {}
