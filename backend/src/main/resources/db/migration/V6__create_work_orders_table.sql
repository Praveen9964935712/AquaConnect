CREATE TABLE work_orders (
    id UUID NOT NULL,
    incident_id UUID NOT NULL,
    assigned_engineer_id UUID,
    status VARCHAR(16) NOT NULL DEFAULT 'CREATED',
    priority VARCHAR(16) NOT NULL DEFAULT 'LOW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT pk_work_orders PRIMARY KEY (id),
    CONSTRAINT fk_work_orders_incident FOREIGN KEY (incident_id) REFERENCES incidents (id),
    CONSTRAINT fk_work_orders_engineer FOREIGN KEY (assigned_engineer_id) REFERENCES users (id),
    CONSTRAINT uk_work_orders_incident UNIQUE (incident_id)
);

CREATE INDEX idx_work_orders_assigned_engineer ON work_orders (assigned_engineer_id);
CREATE INDEX idx_work_orders_status ON work_orders (status);
