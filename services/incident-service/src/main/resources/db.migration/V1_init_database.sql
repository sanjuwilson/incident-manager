CREATE TABLE if not exists incident_rule (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                failure_type VARCHAR(50) NOT NULL,
                                threshold_count INT NOT NULL,
                                window_seconds INT NOT NULL,
                                action VARCHAR(50) NOT NULL,
                                severity VARCHAR(50) NOT NULL,
                                is_enabled BOOLEAN NOT NULL DEFAULT true,
                                created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX uniq_active_incident
    ON incidents (source_service, failure_type)
    WHERE status <> 'CLOSED';

