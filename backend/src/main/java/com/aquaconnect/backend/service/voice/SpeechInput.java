package com.aquaconnect.backend.service.voice;

public record SpeechInput(String audioText, String language, Double confidence) {}
