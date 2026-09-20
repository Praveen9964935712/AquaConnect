package com.aquaconnect.backend.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.verification.AuthorityVerificationRequest;
import com.aquaconnect.backend.dto.verification.AuthorityVerificationResponse;
import com.aquaconnect.backend.entity.AuthorityVerification;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.enums.VerificationDecision;
import com.aquaconnect.backend.exception.InvalidAuthorityVerificationException;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.AuthorityVerificationRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@Service
public class AuthorityVerificationService {

    private final AuthorityVerificationRepository verificationRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;
    private final IncidentLifecycleService incidentLifecycleService;
    private final WorkOrderService workOrderService;

    public AuthorityVerificationService(AuthorityVerificationRepository verificationRepository,
            WorkOrderRepository workOrderRepository, UserRepository userRepository,
            IncidentLifecycleService incidentLifecycleService, WorkOrderService workOrderService) {
        this.verificationRepository = verificationRepository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
        this.incidentLifecycleService = incidentLifecycleService;
        this.workOrderService = workOrderService;
    }

    @Transactional
    public AuthorityVerificationResponse verify(UUID workOrderId, AuthorityVerificationRequest request,
            Authentication authentication) {
        if (!isAuthority(authentication)) {
            throw new InvalidAuthorityVerificationException("User is not authorized to verify repairs");
        }
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found"));
        UUID verifierId = UUID.fromString(authentication.getName());
        if (workOrder.getAssignedEngineer() != null && verifierId.equals(workOrder.getAssignedEngineer().getId())) {
            throw new InvalidAuthorityVerificationException("An engineer cannot approve their own repair");
        }
        if (workOrder.getStatus() != com.aquaconnect.backend.enums.WorkOrderStatus.COMPLETED
                || workOrder.getIncident().getStatus() != IncidentStatus.AUTHORITY_VERIFICATION) {
            throw new InvalidAuthorityVerificationException("Work order is not ready for authority verification");
        }
        if (request.decision() == VerificationDecision.REJECTED
                && (request.notes() == null || request.notes().isBlank())) {
            throw new InvalidAuthorityVerificationException("Rejection notes are required");
        }
        User verifier = userRepository.findById(verifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Verifier not found"));
        if (request.decision() == VerificationDecision.APPROVED) {
            incidentLifecycleService.transition(workOrder.getIncident().getId(), IncidentStatus.RESOLVED);
        } else {
            incidentLifecycleService.transition(workOrder.getIncident().getId(), IncidentStatus.IN_PROGRESS);
            workOrderService.returnForRework(workOrderId);
        }
        return AuthorityVerificationResponse.from(verificationRepository.saveAndFlush(
                new AuthorityVerification(workOrder, workOrder.getIncident(), verifier, request.decision(), request.notes())));
    }

    private boolean isAuthority(Authentication authentication) {
        return authentication.getAuthorities().stream().map(a -> a.getAuthority())
                .map(a -> a.replace("ROLE_", ""))
                .map(RoleName::valueOf)
                .anyMatch(role -> role == RoleName.OPERATOR || role == RoleName.OPERATIONS_MANAGER || role == RoleName.ADMIN);
    }
}
