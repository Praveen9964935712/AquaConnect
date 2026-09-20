package com.aquaconnect.backend.security;

import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.repository.UserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = findUser(identifier);
        return org.springframework.security.core.userdetails.User.withUsername(user.getId().toString())
                .password(user.getPasswordHash())
                .authorities(user.getUserRoles().stream()
                        .map(userRole -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName().name()))
                        .toList())
                .disabled(!user.isActive())
                .build();
    }

    @Transactional(readOnly = true)
    public User findUser(String identifier) {
        try {
            return userRepository.findById(UUID.fromString(identifier))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (IllegalArgumentException exception) {
            return userRepository.findByEmail(identifier.toLowerCase())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }
    }
}
