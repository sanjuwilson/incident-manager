package com.manage.incident_service.incidents;

import com.manage.incident_service.failures.FailureType;

import java.time.Instant;
import java.util.UUID;

public record IncidentNotification(
        UUID id,
        String title,
        String sourceService,
        FailureType failureType,
        Severity severity,
        IncidentStatus status,
        Instant createdAt,
        UUID ruleId
) {
}
