package com.aquaconnect.backend.service.voice;

public record AudioResponse(String id, String format, String language, byte[] payload) {
    public boolean isEmpty() {
        return payload == null || payload.length == 0;
    }
}
