CREATE TABLE ivr_sessions (
    id UUID NOT NULL,
    caller_identifier VARCHAR(100),
    language VARCHAR(16),
    state VARCHAR(32) NOT NULL,
    selected_category VARCHAR(32),
    description VARCHAR(2000),
    incident_id UUID,
    latitude DECIMAL(9,6),
    longitude DECIMAL(10,7),
    location_source VARCHAR(32) NOT NULL,
    location_accuracy DECIMAL(12,3),
    session_status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ivr_sessions PRIMARY KEY (id),
    CONSTRAINT fk_ivr_sessions_incident FOREIGN KEY (incident_id) REFERENCES incidents (id)
);

CREATE INDEX idx_ivr_sessions_caller ON ivr_sessions (caller_identifier);
CREATE INDEX idx_ivr_sessions_status ON ivr_sessions (session_status);
