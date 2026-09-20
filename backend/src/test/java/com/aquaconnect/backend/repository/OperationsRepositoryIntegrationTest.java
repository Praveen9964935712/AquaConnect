package com.aquaconnect.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.SupportingReport;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.enums.WorkOrderStatus;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OperationsRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private SupportingReportRepository supportingReportRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Test
    void persistsSupportingReportAndWorkOrderRelationships() {
        User firstCitizen = userRepository.saveAndFlush(new User(
                "ops_" + UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID() + "@example.com", "hash"));
        User secondCitizen = userRepository.saveAndFlush(new User(
                "ops_" + UUID.randomUUID().toString().substring(0, 8),
                UUID.randomUUID() + "@example.com", "hash"));
        Incident primary = incidentRepository.saveAndFlush(incident(firstCitizen));
        Incident supporting = incidentRepository.saveAndFlush(incident(secondCitizen));

        SupportingReport report = supportingReportRepository.saveAndFlush(new SupportingReport(primary, supporting));
        primary.setStatus(IncidentStatus.VERIFIED);
        incidentRepository.saveAndFlush(primary);
        WorkOrder workOrder = workOrderRepository.saveAndFlush(new WorkOrder(primary, primary.getPriority()));

        assertThat(supportingReportRepository.findById(report.getId())).isPresent();
        assertThat(workOrderRepository.findByIncidentId(primary.getId())).get()
                .extracting(WorkOrder::getStatus).isEqualTo(WorkOrderStatus.CREATED);
        assertThat(workOrder.getIncident().getId()).isEqualTo(primary.getId());
    }

    private Incident incident(User citizen) {
        return new Incident(citizen, IncidentSource.WEB, IncidentCategory.PIPE_LEAK,
                "Water leak report", null, null, LocationSource.UNKNOWN, null);
    }
}
