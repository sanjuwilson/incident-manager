INSERT INTO incident_rule
(failure_type, threshold_count, window_seconds, action, severity, is_enabled, created_at)
VALUES
    ('TIMEOUT', 3, 60, 'CREATE_INCIDENT', 'MEDIUM', true, NOW()),
    ('SERVICE_UNAVAILABLE', 2, 30, 'CREATE_INCIDENT', 'HIGH', true, NOW()),
    ('DB_FAILURE', 1, 60, 'CREATE_INCIDENT', 'CRITICAL', true, NOW()),
    ('AUTH_FAILURE', 5, 120, 'CREATE_INCIDENT', 'HIGH', true, NOW()),
    ('VALIDATION_ERROR', 10, 300, 'LOG_ONLY', 'LOW', true, NOW()),
    ('UNKNOWN', 10, 300, 'IGNORE', 'LOW', true, NOW())
ON CONFLICT (failure_type) DO NOTHING;