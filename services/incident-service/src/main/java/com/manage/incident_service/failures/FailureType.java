package com.manage.incident_service.failures;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


public enum FailureType {
    SERVICE_UNAVAILABLE,
    TIMEOUT,
    DB_FAILURE,
    AUTH_FAILURE,
    VALIDATION_ERROR,
    UNKNOWN
}