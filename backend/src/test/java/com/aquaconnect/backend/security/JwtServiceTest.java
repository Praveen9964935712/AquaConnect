package com.aquaconnect.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aquaconnect.backend.entity.User;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-only-secret-that-is-at-least-32-characters-long", 3600);
    }

    @Test
    void generatedTokenContainsUserIdentityAndRoles() {
        User user = new User("citizen01", "citizen@example.com", "hash");
        setId(user, UUID.randomUUID());

        String token = jwtService.generateToken(user, List.of("CITIZEN"));

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void malformedTokenIsInvalid() {
        assertThat(jwtService.isValid("not-a-token")).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        JwtService expiredJwtService = new JwtService(
                "test-only-secret-that-is-at-least-32-characters-long", -1);
        User user = new User("citizen01", "citizen@example.com", "hash");
        setId(user, UUID.randomUUID());

        assertThat(expiredJwtService.isValid(expiredJwtService.generateToken(user, List.of("CITIZEN")))).isFalse();
    }

    private static void setId(User user, UUID id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
