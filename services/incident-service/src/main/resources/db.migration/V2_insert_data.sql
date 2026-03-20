INSERT INTO incident_rule
(id, failure_type, threshold_count, window_seconds, action, severity, is_enabled, created_at)
VALUES
    (gen_random_uuid(), 'TIMEOUT', 3, 60, 'CREATE_INCIDENT', 'MEDIUM', true, NOW()),
    (gen_random_uuid(), 'SERVICE_UNAVAILABLE', 2, 30, 'CREATE_INCIDENT', 'HIGH', true, NOW()),
    (gen_random_uuid(), 'DB_FAILURE', 1, 60, 'CREATE_INCIDENT', 'CRITICAL', true, NOW()),
    (gen_random_uuid(), 'AUTH_FAILURE', 5, 120, 'CREATE_INCIDENT', 'HIGH', true, NOW()),
    (gen_random_uuid(), 'VALIDATION_ERROR', 10, 300, 'LOG_ONLY', 'LOW', true, NOW()),
    (gen_random_uuid(), 'UNKNOWN', 10, 300, 'IGNORE', 'LOW', true, NOW());