CREATE TABLE ivr_escalation_requests (
    id UUID NOT NULL,
    session_id UUID NOT NULL,
    caller_identifier VARCHAR(100) NOT NULL,
    language VARCHAR(16),
    reason VARCHAR(1000),
    status VARCHAR(24) NOT NULL,
    assigned_operator_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT pk_ivr_escalation_requests PRIMARY KEY (id)
);

CREATE INDEX idx_ivr_escalation_requests_session ON ivr_escalation_requests (session_id);
CREATE INDEX idx_ivr_escalation_requests_caller ON ivr_escalation_requests (caller_identifier);
CREATE INDEX idx_ivr_escalation_requests_status ON ivr_escalation_requests (status);
