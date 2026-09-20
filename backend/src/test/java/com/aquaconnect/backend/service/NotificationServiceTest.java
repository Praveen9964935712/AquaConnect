package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.aquaconnect.backend.dto.notification.NotificationResponse;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.Notification;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.enums.NotificationType;
import com.aquaconnect.backend.repository.NotificationRepository;
import com.aquaconnect.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);
    }

    @Test
    void createsNotificationForCorrectRecipient() {
        User user = new User("citizen", "citizen@example.com", "hash");
        Notification notification = new Notification(user, NotificationType.INCIDENT_CREATED,
                "Incident created", "message", null, null);
        when(notificationRepository.saveAndFlush(any(Notification.class))).thenReturn(notification);

        Notification created = notificationService.createForUser(user, NotificationType.INCIDENT_CREATED,
                "Incident created", "message", null, null);

        assertThat(created.getRecipient().getId()).isEqualTo(user.getId());
        assertThat(created.getType()).isEqualTo(NotificationType.INCIDENT_CREATED);
    }

    @Test
    void listsOnlyCurrentUserNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User("citizen", "citizen@example.com", "hash");
        setId(user, userId);
        Notification notification = new Notification(user, NotificationType.INCIDENT_RESOLVED,
                "Resolved", "message", null, null);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(notification));

        var auth = UsernamePasswordAuthenticationToken.authenticated(userId.toString(), null, List.of());
        List<NotificationResponse> responses = notificationService.listForCurrentUser(auth);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo(NotificationType.INCIDENT_RESOLVED);
    }

    @Test
    void marksNotificationRead() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User("citizen", "citizen@example.com", "hash");
        setId(user, userId);
        Notification notification = new Notification(user, NotificationType.INCIDENT_CLOSED,
                "Closed", "message", null, null);
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        var auth = UsernamePasswordAuthenticationToken.authenticated(userId.toString(), null, List.of());
        NotificationResponse response = notificationService.markRead(notification.getId(), auth);

        assertThat(response.read()).isTrue();
        verify(notificationRepository).save(notification);
    }

    private Incident createIncident(User citizen) {
        return new Incident(citizen, IncidentSource.WEB, IncidentCategory.PIPE_LEAK,
                "Leak", null, null, LocationSource.UNKNOWN, null);
    }

    private void setId(Object target, UUID value) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, value);
    }
}
