package com.manage.incident_service.rules;

import com.manage.incident_service.failures.FailureType;
import com.manage.incident_service.incidents.Severity;

import java.time.Instant;
import java.util.UUID;

public record IncidentRuleDto(
        UUID id,
        FailureType failureType,
        int thresholdCount,
        int windowSeconds,
        IncidentAction action,
        Severity severity,
        boolean enabled,
        Instant createdAt
) {}
