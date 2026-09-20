package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aquaconnect.backend.dto.auth.AuthResponse;
import com.aquaconnect.backend.dto.auth.RegisterRequest;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.repository.RoleRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.security.DatabaseUserDetailsService;
import com.aquaconnect.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private DatabaseUserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                userDetailsService,
                new JwtService("test-only-secret-that-is-at-least-32-characters-long", 3600));
    }

    @Test
    void registrationHashesPasswordAndAssignsCitizenRole() throws Exception {
        Role citizen = new Role(RoleName.CITIZEN, "Citizen user");
        User savedUser = new User("citizen", "citizen@example.com", "hash");
        setId(savedUser, UUID.randomUUID());
        when(userRepository.findByEmail("citizen@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.CITIZEN)).thenReturn(Optional.of(citizen));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(new RegisterRequest(
                "citizen@example.com", "Citizen password 1", "Citizen", null, null));

        var captured = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captured.capture());
        User persisted = captured.getValue();
        assertThat(persisted.getPasswordHash()).isNotEqualTo("Citizen password 1");
        assertThat(passwordEncoder.matches("Citizen password 1", persisted.getPasswordHash())).isTrue();
        assertThat(persisted.getUserRoles()).extracting(userRole -> userRole.getRole().getName())
                .containsExactly(RoleName.CITIZEN);
        assertThat(response.email()).isEqualTo("citizen@example.com");
    }

    @Test
    void loginAuthenticatesUsingEmail() {
        UUID userId = UUID.randomUUID();
        User user = new User("citizen", "citizen@example.com", passwordEncoder.encode("Citizen password 1"));
        setIdUnchecked(user, userId);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(userId.toString(), null, java.util.List.of()));
        when(userDetailsService.findUser(userId.toString())).thenReturn(user);

        AuthResponse response = authService.login(new com.aquaconnect.backend.dto.auth.LoginRequest(
                "citizen@example.com", "Citizen password 1"));

        assertThat(response.token()).isNotBlank();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    private static void setId(User user, UUID id) throws Exception {
        setIdUnchecked(user, id);
    }

    private static void setIdUnchecked(User user, UUID id) {
        try {
            Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
