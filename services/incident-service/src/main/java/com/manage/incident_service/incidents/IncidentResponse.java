package com.manage.incident_service.incidents;

import com.manage.incident_service.failures.FailureType;



import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        String title,
        String sourceService,
        FailureType failureType,
        Severity severity,
        IncidentStatus status,
        Instant createdAt,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Instant closedAt,
        String assignedTo
) {

}