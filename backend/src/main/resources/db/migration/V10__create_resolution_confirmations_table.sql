CREATE TABLE resolution_confirmations (
    id UUID NOT NULL,
    incident_id UUID NOT NULL,
    citizen_id UUID NOT NULL,
    decision VARCHAR(16) NOT NULL,
    comment VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_resolution_confirmations PRIMARY KEY (id),
    CONSTRAINT fk_resolution_confirmations_incident FOREIGN KEY (incident_id) REFERENCES incidents (id),
    CONSTRAINT fk_resolution_confirmations_citizen FOREIGN KEY (citizen_id) REFERENCES users (id),
    CONSTRAINT chk_resolution_confirmation_still_present_comment CHECK (decision <> 'STILL_PRESENT' OR (comment IS NOT NULL AND char_length(trim(comment)) > 0))
);

CREATE INDEX idx_resolution_confirmations_incident ON resolution_confirmations (incident_id);
CREATE INDEX idx_resolution_confirmations_citizen ON resolution_confirmations (citizen_id);
