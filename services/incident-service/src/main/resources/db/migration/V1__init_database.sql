CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS incident_rule (
                                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                             failure_type VARCHAR(50) NOT NULL,
                                             threshold_count INT NOT NULL,
                                             window_seconds INT NOT NULL,
                                             action VARCHAR(50) NOT NULL,
                                             severity VARCHAR(50) NOT NULL,
                                             is_enabled BOOLEAN NOT NULL DEFAULT true,
                                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_incident_rule_failure_type
    ON incident_rule (failure_type);

ALTER TABLE incident_rule
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

SELECT
    column_name,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'incident_rule'
  AND column_name = 'id';