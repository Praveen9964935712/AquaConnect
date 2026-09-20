CREATE TABLE authority_verifications (
    id UUID NOT NULL,
    work_order_id UUID NOT NULL,
    incident_id UUID NOT NULL,
    verified_by_user_id UUID NOT NULL,
    decision VARCHAR(16) NOT NULL,
    notes VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_authority_verifications PRIMARY KEY (id),
    CONSTRAINT fk_authority_verifications_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders (id),
    CONSTRAINT fk_authority_verifications_incident FOREIGN KEY (incident_id) REFERENCES incidents (id),
    CONSTRAINT fk_authority_verifications_user FOREIGN KEY (verified_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_authority_rejection_notes CHECK (decision <> 'REJECTED' OR (notes IS NOT NULL AND char_length(trim(notes)) > 0))
);

CREATE INDEX idx_authority_verifications_work_order ON authority_verifications (work_order_id);
CREATE INDEX idx_authority_verifications_incident ON authority_verifications (incident_id);
