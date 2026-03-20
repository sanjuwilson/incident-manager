package com.manage.notification.notification_service.notification;

import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.incidents.Severity;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID incidentId,
        String title,
        String recipient,
        NotificationStatus status,
        String sourceService,
        String failureReason,
        Severity severity,
        IncidentStatus incidentStatus,
        int retryCount,
        Instant createdAt,
        Instant sentAt
) {}
