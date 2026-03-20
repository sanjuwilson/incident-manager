package com.manage.notification.notification_service.notification;

import com.manage.incident_service.incidents.IncidentStatus;
import com.manage.incident_service.incidents.Severity;

import java.time.Instant;
import java.util.UUID;

public record SendNotification(
        UUID incidentId,
        String title,
        String sourceService,
        String failureReason,
        Instant createdAt,
        Severity severity,
        Instant sentAt,
        IncidentStatus incidentStatus,
        String recipient){
}
