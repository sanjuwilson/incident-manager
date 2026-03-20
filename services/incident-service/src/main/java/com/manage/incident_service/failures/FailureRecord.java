package com.manage.incident_service.failures;

import java.time.Instant;

public record FailureRecord(
        String sourceService,
        FailureType failureType,
        String operation,
        String dependency,
        Instant occurredAt,
        String correlationId
) {}
