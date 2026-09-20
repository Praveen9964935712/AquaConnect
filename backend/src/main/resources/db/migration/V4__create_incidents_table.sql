CREATE TABLE incidents (
    id UUID NOT NULL,
    citizen_id UUID,
    source VARCHAR(16) NOT NULL,
    category VARCHAR(32) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(10, 7),
    location_source VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    location_accuracy NUMERIC(12, 3),
    reported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    priority VARCHAR(16) NOT NULL DEFAULT 'LOW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_incidents PRIMARY KEY (id),
    CONSTRAINT fk_incidents_citizen FOREIGN KEY (citizen_id) REFERENCES users (id),
    CONSTRAINT chk_incidents_description_not_blank CHECK (char_length(trim(description)) > 0),
    CONSTRAINT chk_incidents_latitude_range CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_incidents_longitude_range CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180)),
    CONSTRAINT chk_incidents_location_accuracy_non_negative CHECK (location_accuracy IS NULL OR location_accuracy >= 0)
);

CREATE INDEX idx_incidents_citizen_id ON incidents (citizen_id);
CREATE INDEX idx_incidents_status ON incidents (status);
CREATE INDEX idx_incidents_created_at ON incidents (created_at);