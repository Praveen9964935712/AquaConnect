ALTER TABLE incidents
    ADD COLUMN caller_identifier VARCHAR(100);

CREATE INDEX idx_incidents_caller_identifier ON incidents (caller_identifier);
