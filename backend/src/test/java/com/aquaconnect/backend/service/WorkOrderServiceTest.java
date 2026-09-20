package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.enums.WorkOrderStatus;
import com.aquaconnect.backend.exception.InvalidWorkOrderException;
import com.aquaconnect.backend.exception.InvalidWorkOrderTransitionException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IncidentLifecycleService incidentLifecycleService;

    private WorkOrderService service;
    private UUID incidentId;
    private Incident incident;

    @BeforeEach
    void setUp() {
        service = new WorkOrderService(workOrderRepository, incidentRepository, userRepository, incidentLifecycleService);
        incidentId = UUID.randomUUID();
        incident = new Incident(null, IncidentSource.WEB, IncidentCategory.PIPE_LEAK,
                "Description", null, null, LocationSource.UNKNOWN, null);
        incident.setStatus(IncidentStatus.VERIFIED);
        lenient().when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        lenient().when(workOrderRepository.findByIncidentId(incidentId)).thenReturn(Optional.empty());
        lenient().when(workOrderRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(workOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsOnlyForVerifiedIncident() {
        service.create(incidentId);

        incident.setStatus(IncidentStatus.SUBMITTED);
        assertThatThrownBy(() -> service.create(incidentId)).isInstanceOf(InvalidWorkOrderException.class);
    }

    @Test
    void rejectsNonEngineerAssignment() {
        UUID workOrderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User operator = new User("operator", "operator@example.com", "hash");
        Role role = new Role(RoleName.OPERATOR, "Operator");
        operator.getUserRoles().add(new UserRole(operator, role));
        when(workOrderRepository.findById(workOrderId)).thenReturn(Optional.of(
            new com.aquaconnect.backend.entity.WorkOrder(incident, com.aquaconnect.backend.enums.IncidentPriority.LOW)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(operator));

        assertThatThrownBy(() -> service.assign(workOrderId, userId)).isInstanceOf(InvalidWorkOrderException.class);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        UUID workOrderId = UUID.randomUUID();
        com.aquaconnect.backend.entity.WorkOrder workOrder = new com.aquaconnect.backend.entity.WorkOrder(
                incident, com.aquaconnect.backend.enums.IncidentPriority.LOW);
        when(workOrderRepository.findById(workOrderId)).thenReturn(Optional.of(workOrder));
        var authentication = UsernamePasswordAuthenticationToken.authenticated(UUID.randomUUID().toString(), null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_OPERATOR")));

        assertThatThrownBy(() -> service.transition(workOrderId, WorkOrderStatus.COMPLETED, authentication))
                .isInstanceOf(InvalidWorkOrderTransitionException.class);
    }
}
