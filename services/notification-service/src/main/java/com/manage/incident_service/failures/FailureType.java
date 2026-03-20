package com.manage.incident_service.failures;


public enum FailureType {
    SERVICE_UNAVAILABLE,
    TIMEOUT,
    DB_FAILURE,
    AUTH_FAILURE,
    VALIDATION_ERROR,
    UNKNOWN
}