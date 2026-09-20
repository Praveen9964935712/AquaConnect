package com.aquaconnect.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.auth.AuthResponse;
import com.aquaconnect.backend.dto.auth.LoginRequest;
import com.aquaconnect.backend.dto.auth.RegisterRequest;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.repository.RoleRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.security.DatabaseUserDetailsService;
import com.aquaconnect.backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final DatabaseUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            DatabaseUserDetailsService userDetailsService,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DataIntegrityViolationException("Email is already registered");
        }

        String username = createUsername(email);
        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName(),
                request.displayName());
        Role citizenRole = roleRepository.findByName(RoleName.CITIZEN)
            .orElseGet(() -> roleRepository.save(new Role(RoleName.CITIZEN, "Citizen user")));
        user.getUserRoles().add(new UserRole(user, citizenRole));
        User savedUser = userRepository.saveAndFlush(user);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(Locale.ROOT), request.password()));
        User user = userDetailsService.findUser(authentication.getName());
        return toResponse(user);
    }

    private String createUsername(String email) {
        String base = email.substring(0, email.indexOf('@'));
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.length() < 3) {
            base = "user";
        }
        base = base.substring(0, Math.min(base.length(), 42));
        String username = base;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            String suffixText = "_" + suffix++;
            username = base.substring(0, Math.min(base.length(), 50 - suffixText.length())) + suffixText;
        }
        return username;
    }

    private AuthResponse toResponse(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().name())
                .sorted()
                .toList();
        return new AuthResponse(jwtService.generateToken(user, roles), user.getId(), user.getEmail(), roles);
    }
}
