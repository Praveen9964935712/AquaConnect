package com.aquaconnect.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.notification.NotificationResponse;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.Notification;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.NotificationType;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.NotificationRepository;
import com.aquaconnect.backend.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createForUser(User recipient, NotificationType type, String title, String message,
            Incident relatedIncident, WorkOrder relatedWorkOrder) {
        return notificationRepository.saveAndFlush(new Notification(recipient, type, title, message, relatedIncident, relatedWorkOrder));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listUnreadForCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID notificationId, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.markRead();
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    public void notifyIncidentCreated(Incident incident) {
        createForUser(incident.getCitizen(), NotificationType.INCIDENT_CREATED,
                "Incident created", "Your incident has been submitted and is being reviewed.", incident, null);
    }

    public void notifyIncidentVerified(Incident incident) {
        createForUser(incident.getCitizen(), NotificationType.INCIDENT_VERIFIED,
                "Incident verified", "Your reported issue has been verified and is being assigned.", incident, null);
    }

    public void notifyWorkOrderAssigned(WorkOrder workOrder) {
        if (workOrder.getAssignedEngineer() != null) {
            createForUser(workOrder.getAssignedEngineer(), NotificationType.WORK_ORDER_ASSIGNED,
                    "Work order assigned", "You have been assigned a new incident work order.",
                    workOrder.getIncident(), workOrder);
        }
    }

    public void notifyWorkStarted(WorkOrder workOrder) {
        if (workOrder.getAssignedEngineer() != null) {
            createForUser(workOrder.getAssignedEngineer(), NotificationType.WORK_STARTED,
                    "Work started", "You have started the assigned repair work.",
                    workOrder.getIncident(), workOrder);
        }
    }

    public void notifyRepairCompleted(WorkOrder workOrder) {
        createForUser(workOrder.getIncident().getCitizen(), NotificationType.REPAIR_COMPLETED,
                "Repair completed", "The repair has been completed and is awaiting authority verification.",
                workOrder.getIncident(), workOrder);
    }

    public void notifyAuthorityRejected(WorkOrder workOrder, String notes) {
        createForUser(workOrder.getAssignedEngineer(), NotificationType.AUTHORITY_REJECTED,
                "Repair rejected", notes == null ? "The repair was rejected and requires attention." : notes,
                workOrder.getIncident(), workOrder);
    }

    public void notifyIncidentResolved(Incident incident) {
        createForUser(incident.getCitizen(), NotificationType.INCIDENT_RESOLVED,
                "Incident resolved", "Your issue has been resolved and requires your confirmation.", incident, null);
    }

    public void notifyIncidentReopened(Incident incident) {
        createForUser(incident.getCitizen(), NotificationType.INCIDENT_REOPENED,
                "Incident reopened", "Your issue has been reopened for further review.", incident, null);
    }

    public void notifyIncidentClosed(Incident incident) {
        createForUser(incident.getCitizen(), NotificationType.INCIDENT_CLOSED,
                "Incident closed", "Your issue has been closed after confirmation.", incident, null);
    }
}
