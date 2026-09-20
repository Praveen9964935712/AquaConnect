package com.aquaconnect.backend.controller;

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
class IncidentControllerIntegrationTest {

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
    void citizenCanCreateAndOnlyViewOwnIncidents() throws Exception {
        String firstToken = registerAndGetToken();
        String secondToken = registerAndGetToken();

        MvcResult createResult = mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + firstToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validIncidentJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("WEB"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.citizen").doesNotExist())
                .andReturn();
        String incidentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/incidents").header("Authorization", "Bearer " + firstToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(incidentId));
        mockMvc.perform(get("/api/incidents").header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/incidents/" + incidentId).header("Authorization", "Bearer " + secondToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnauthenticatedInvalidAndIvrCitizenRequests() throws Exception {
        mockMvc.perform(get("/api/incidents")).andExpect(status().isUnauthorized());

        String token = registerAndGetToken();
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"missing category\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"PIPE_LEAK\",\"description\":\"bad latitude\",\"latitude\":91}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/incidents")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validIncidentJson().replace("\"locationSource\":\"GPS\"", "\"locationSource\":\"GPS\",\"source\":\"IVR\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonCitizenCannotUseCitizenIncidentApi() throws Exception {
        String token = createOperatorToken();

        mockMvc.perform(get("/api/incidents").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetToken() throws Exception {
        String email = "incident-api-" + UUID.randomUUID() + "@example.com";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Citizen password 1\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String createOperatorToken() throws Exception {
        String email = "operator-" + UUID.randomUUID() + "@example.com";
        Role operator = roleRepository.findByName(RoleName.OPERATOR)
                .orElseGet(() -> roleRepository.saveAndFlush(new Role(RoleName.OPERATOR, "Control-room operator")));
        User user = new User("operator_" + UUID.randomUUID().toString().substring(0, 8), email,
                passwordEncoder.encode("Operator password 1"));
        user.getUserRoles().add(new UserRole(user, operator));
        userRepository.saveAndFlush(user);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Operator password 1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    private static String validIncidentJson() {
        return "{\"category\":\"PIPE_LEAK\",\"description\":\"Water leaking near the junction\","
                + "\"latitude\":12.9716,\"longitude\":77.5946,\"locationSource\":\"GPS\",\"locationAccuracy\":5}";
    }
}
