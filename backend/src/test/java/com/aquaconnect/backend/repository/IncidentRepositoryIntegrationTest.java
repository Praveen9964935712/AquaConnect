package com.aquaconnect.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IncidentRepositoryIntegrationTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void persistsIncidentWithUserEnumsLocationStatusAndPriority() {
        User citizen = userRepository.saveAndFlush(new User(
                "incident_" + UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID() + "@example.com",
                "placeholder-hash"));
        Incident incident = new Incident(
                citizen,
                IncidentSource.WEB,
                IncidentCategory.PIPE_LEAK,
                "Water is leaking near the junction",
                new BigDecimal("12.971600"),
                new BigDecimal("77.5946000"),
                LocationSource.GPS,
                new BigDecimal("4.500"));

        Incident persisted = incidentRepository.saveAndFlush(incident);

        Incident reloaded = incidentRepository.findByIdAndCitizenId(persisted.getId(), citizen.getId()).orElseThrow();
        assertThat(reloaded.getCitizen().getId()).isEqualTo(citizen.getId());
        assertThat(reloaded.getSource()).isEqualTo(IncidentSource.WEB);
        assertThat(reloaded.getCategory()).isEqualTo(IncidentCategory.PIPE_LEAK);
        assertThat(reloaded.getDescription()).isEqualTo("Water is leaking near the junction");
        assertThat(reloaded.getLatitude()).isEqualByComparingTo("12.971600");
        assertThat(reloaded.getLongitude()).isEqualByComparingTo("77.5946000");
        assertThat(reloaded.getLocationSource()).isEqualTo(LocationSource.GPS);
        assertThat(reloaded.getLocationAccuracy()).isEqualByComparingTo("4.500");
        assertThat(reloaded.getStatus()).isEqualTo(IncidentStatus.SUBMITTED);
        assertThat(reloaded.getPriority()).isEqualTo(IncidentPriority.LOW);
        assertThat(reloaded.getReportedAt()).isNotNull();
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getUpdatedAt()).isNotNull();
    }
}
