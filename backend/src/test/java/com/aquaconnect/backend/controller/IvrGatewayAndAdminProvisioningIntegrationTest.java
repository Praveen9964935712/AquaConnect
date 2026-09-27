package com.aquaconnect.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.repository.RoleRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "app.jwt.secret=test-only-secret-that-is-at-least-32-characters-long",
        "app.ivr.trust-token=dev-ivr-test-token"
})
@AutoConfigureMockMvc
class IvrGatewayAndAdminProvisioningIntegrationTest {

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
    void ivrGatewayRequiresValidTrustAndOnlyAllowsOwnerSessionAccess() throws Exception {
        String email = "citizen-ivr-" + UUID.randomUUID() + "@example.com";
        String password = "Citizen password 1";
        register(email, password);

        String token = login(email, password);

        mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"caller-a\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"caller-a\"}")
                        .header("X-IVR-Trust", "bad-token")
                        .header("X-IVR-Caller-Id", "caller-a"))
                .andExpect(status().isUnauthorized());

        MvcResult createSession = mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"caller-a\"}")
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "caller-a"))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(createSession.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "caller-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.callerIdentifier").value("caller-a"));

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "caller-b"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}/complaints", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "caller-b"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/manager/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void adminCanProvisionOperatorRoleWithoutAllowingPublicRoleEscalation() throws Exception {
        String adminEmail = "admin-provision-" + UUID.randomUUID() + "@example.com";
        String adminPassword = "Admin password 1";
        register(adminEmail, adminPassword);

        User adminUser = userRepository.findByEmail(adminEmail).orElseThrow();
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.saveAndFlush(new Role(RoleName.ADMIN, "Administrator")));
        adminUser.getUserRoles().add(new UserRole(adminUser, adminRole));
        userRepository.saveAndFlush(adminUser);

        String adminToken = login(adminEmail, adminPassword);
        String targetEmail = "operator-target-" + UUID.randomUUID() + "@example.com";
        register(targetEmail, "Operator password 1");
        User targetUser = userRepository.findByEmail(targetEmail).orElseThrow();

        mockMvc.perform(post("/api/admin/users/{userId}/roles", targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"OPERATOR\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + targetEmail + "\",\"password\":\"Operator password 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").value(org.hamcrest.Matchers.hasItem("OPERATOR")));
   }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }
}
