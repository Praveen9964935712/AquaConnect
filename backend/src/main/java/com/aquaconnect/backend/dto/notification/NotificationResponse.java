package com.aquaconnect.backend.dto.notification;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.Notification;
import com.aquaconnect.backend.enums.NotificationType;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        NotificationType type,
        String title,
        String message,
        UUID relatedIncidentId,
        UUID relatedWorkOrderId,
        boolean read,
        Instant createdAt,
        Instant readAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipient().getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedIncident() == null ? null : notification.getRelatedIncident().getId(),
                notification.getRelatedWorkOrder() == null ? null : notification.getRelatedWorkOrder().getId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
