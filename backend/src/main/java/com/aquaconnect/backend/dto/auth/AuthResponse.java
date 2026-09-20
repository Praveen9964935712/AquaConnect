package com.aquaconnect.backend.dto.auth;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String email,
        List<String> roles) {
}
