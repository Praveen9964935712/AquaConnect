CREATE TABLE supporting_reports (
    id UUID NOT NULL,
    primary_incident_id UUID NOT NULL,
    supporting_incident_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_supporting_reports PRIMARY KEY (id),
    CONSTRAINT fk_supporting_reports_primary FOREIGN KEY (primary_incident_id) REFERENCES incidents (id) ON DELETE CASCADE,
    CONSTRAINT fk_supporting_reports_supporting FOREIGN KEY (supporting_incident_id) REFERENCES incidents (id) ON DELETE CASCADE,
    CONSTRAINT uk_supporting_reports_pair UNIQUE (primary_incident_id, supporting_incident_id),
    CONSTRAINT chk_supporting_reports_not_self CHECK (primary_incident_id <> supporting_incident_id)
);

CREATE INDEX idx_supporting_reports_primary ON supporting_reports (primary_incident_id);
CREATE INDEX idx_supporting_reports_supporting ON supporting_reports (supporting_incident_id);
