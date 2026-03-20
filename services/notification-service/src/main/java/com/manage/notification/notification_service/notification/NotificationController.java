package com.manage.notification.notification_service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @PreAuthorize("hasRole('admin')or hasRole('user')")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @RequestParam(required = false) UUID incidentId,
            @RequestParam(required = false) NotificationStatus status) {
        return ResponseEntity.ok(notificationService.getAll(incidentId, status));
    }
    @PreAuthorize("hasRole('admin')or hasRole('user')")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }
    @PreAuthorize("hasRole('admin')or hasRole('user')")
    @GetMapping("/incident/{incidentId}")
    public ResponseEntity<List<NotificationResponse>> getByIncidentId(
            @PathVariable UUID incidentId) {
        return ResponseEntity.ok(notificationService.getByIncidentId(incidentId));
    }
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationResponse> retry(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.retry(id));
    }
}