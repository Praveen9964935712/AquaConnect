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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

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
class IvrPhase45IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void publicIvrGatewayRejectsInvalidTrustAndCrossCallerAccess() throws Exception {
        String callerId = "caller-p45-" + UUID.randomUUID();

        mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"" + callerId + "\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"" + callerId + "\"}")
                        .header("X-IVR-Trust", "bad-token")
                        .header("X-IVR-Caller-Id", callerId))
                .andExpect(status().isUnauthorized());

        MvcResult sessionResult = mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"" + callerId + "\"}")
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", callerId))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "other-caller"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}/complaints", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "other-caller"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void ivrReportingFlowCreatesIncidentAndOwnerOnlyComplaintStatusesAreReturned() throws Exception {
        String email = "ivr-phase45-" + UUID.randomUUID() + "@example.com";
        String password = "Citizen password 1";
        register(email, password);

        String adminToken = loginWithRole(email, password, RoleName.ADMIN);
        String callerId = "ivr-citizen-" + UUID.randomUUID();

        MvcResult sessionResult = mockMvc.perform(post("/api/ivr/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"" + callerId + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/ivr/sessions/{id}/language", sessionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"ENGLISH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("ENGLISH"));

        mockMvc.perform(post("/api/ivr/sessions/{id}/menu", sessionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"choice\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REPORT_PROBLEM"));

        mockMvc.perform(post("/api/ivr/sessions/{id}/details", sessionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"PIPE_LEAK\",\"description\":\"Water leak near market road\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.selectedCategory").value("PIPE_LEAK"));

        mockMvc.perform(post("/api/ivr/sessions/{id}/location", sessionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"latitude\":12.9716,\"longitude\":77.5946,\"locationSource\":\"GPS\",\"locationAccuracy\":8.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(12.9716));

        MvcResult confirmResult = mockMvc.perform(post("/api/ivr/sessions/{id}/confirmation", sessionId)
                        .header("Authorization", "Bearer " + adminToken)
                        .param("confirmed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId").isNotEmpty())
                .andReturn();

        String incidentId = objectMapper.readTree(confirmResult.getResponse().getContentAsString()).get("incidentId").asText();
        assertThat(incidentId).isNotBlank();

        MvcResult statusSessionResult = mockMvc.perform(post("/api/ivr/gateway/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"" + callerId + "\"}")
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", callerId))
                .andExpect(status().isCreated())
                .andReturn();
        String complaintSessionId = objectMapper.readTree(statusSessionResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}/complaints", complaintSessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", callerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));

        mockMvc.perform(get("/api/ivr/gateway/sessions/{id}/complaints", complaintSessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "other-caller"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void ivrEscalationFlowQueuesOnlyForActiveOwnerSessionAndRejectsInvalidTransitions() throws Exception {
        String email = "ivr-escalation-" + UUID.randomUUID() + "@example.com";
        String password = "Citizen password 1";
        register(email, password);

        String adminToken = loginWithRole(email, password, RoleName.ADMIN);
        String callerId = "ivr-escalation-caller-" + UUID.randomUUID();

        MvcResult sessionResult = mockMvc.perform(post("/api/ivr/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"callerIdentifier\":\"" + callerId + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/ivr/gateway/sessions/{id}/escalate", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", callerId)
                        .param("language", "ENGLISH")
                        .param("reason", "Need a live operator."))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.callerIdentifier").value(callerId))
                .andExpect(jsonPath("$.status").value("QUEUED"));

        mockMvc.perform(post("/api/ivr/gateway/sessions/{id}/escalate", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", callerId)
                        .param("language", "ENGLISH")
                        .param("reason", "Need a live operator."))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/ivr/gateway/sessions/{id}/escalate", sessionId)
                        .header("X-IVR-Trust", "dev-ivr-test-token")
                        .header("X-IVR-Caller-Id", "other-caller")
                        .param("language", "ENGLISH")
                        .param("reason", "Need a live operator."))
                .andExpect(status().isForbidden());
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());
    }

    private String loginWithRole(String email, String password, RoleName role) throws Exception {
        User user = userRepository.findByEmail(email).orElseThrow();
        Role roleEntity = roleRepository.findByName(role)
                .orElseGet(() -> roleRepository.saveAndFlush(new Role(role, role.name())));
        boolean alreadyAssigned = user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName() == role);
        if (!alreadyAssigned) {
            user.getUserRoles().add(new UserRole(user, roleEntity));
            userRepository.saveAndFlush(user);
        }

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }
}
