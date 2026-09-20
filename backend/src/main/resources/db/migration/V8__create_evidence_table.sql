CREATE TABLE evidence (
    id UUID NOT NULL,
    work_order_id UUID NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    evidence_type VARCHAR(32) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_evidence PRIMARY KEY (id),
    CONSTRAINT fk_evidence_work_order FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id),
    CONSTRAINT chk_evidence_file_size_positive CHECK (file_size > 0)
);

CREATE INDEX idx_evidence_work_order_id ON evidence (work_order_id);
CREATE INDEX idx_evidence_uploaded_by ON evidence (uploaded_by_user_id);