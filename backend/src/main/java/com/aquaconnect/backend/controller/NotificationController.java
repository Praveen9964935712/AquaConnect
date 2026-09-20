package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.notification.NotificationResponse;
import com.aquaconnect.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(Authentication authentication) {
        return notificationService.listForCurrentUser(authentication);
    }

    @GetMapping("/unread")
    public List<NotificationResponse> unread(Authentication authentication) {
        return notificationService.listUnreadForCurrentUser(authentication);
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id, Authentication authentication) {
        return notificationService.markRead(id, authentication);
    }
}
