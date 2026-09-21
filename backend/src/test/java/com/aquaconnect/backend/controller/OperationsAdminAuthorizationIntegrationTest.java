package com.aquaconnect.backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.repository.RoleRepository;
import com.aquaconnect.backend.repository.UserRepository;

@SpringBootTest(properties = "app.jwt.secret=test-only-secret-that-is-at-least-32-characters-long")
@AutoConfigureMockMvc
class OperationsAdminAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void managerDashboardRequiresManagerOrAdmin() throws Exception {
        mockMvc.perform(get("/api/manager/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/manager/dashboard").header("Authorization", "Bearer " + token(RoleName.OPERATOR)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/manager/dashboard").header("Authorization", "Bearer " + token(RoleName.OPERATIONS_MANAGER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.incidentsByStatus").exists());
    }

    @Test
    void adminViewsAreAdminOnlyAndNeverExposePasswords() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token(RoleName.OPERATIONS_MANAGER)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token(RoleName.ADMIN)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void documentationIsPublicButBusinessEndpointsRemainProtected() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.info.title").value("AquaConnect API"));
        mockMvc.perform(get("/api/admin/roles")).andExpect(status().isUnauthorized());
    }

    private String token(RoleName roleName) throws Exception {
        String email = roleName.name().toLowerCase() + "-" + UUID.randomUUID() + "@example.com";
        Role role = roleRepository.findByName(roleName).orElseGet(() -> roleRepository.saveAndFlush(new Role(roleName, roleName.name())));
        User user = new User(roleName.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 8), email,
                passwordEncoder.encode("Password 123"));
        user.getUserRoles().add(new UserRole(user, role));
        userRepository.saveAndFlush(user);
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password 123\"}"))
                .andExpect(status().isOk()).andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }
}
