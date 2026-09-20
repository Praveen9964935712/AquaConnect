CREATE TABLE water_zones (
    id UUID NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_water_zones PRIMARY KEY (id),
    CONSTRAINT uk_water_zones_code UNIQUE (code)
);

CREATE TABLE infrastructure_assets (
    id UUID NOT NULL,
    asset_type VARCHAR(32) NOT NULL,
    name VARCHAR(200) NOT NULL,
    identifier VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    latitude DECIMAL(9,6),
    longitude DECIMAL(10,7),
    geom geometry(Point,4326),
    zone_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_infrastructure_assets PRIMARY KEY (id),
    CONSTRAINT fk_infrastructure_assets_zone FOREIGN KEY (zone_id) REFERENCES water_zones (id),
    CONSTRAINT chk_infrastructure_assets_latitude CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90)),
    CONSTRAINT chk_infrastructure_assets_longitude CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180))
);

CREATE INDEX idx_infrastructure_assets_zone ON infrastructure_assets (zone_id);
CREATE INDEX idx_infrastructure_assets_active ON infrastructure_assets (active);
CREATE INDEX idx_infrastructure_assets_geom ON infrastructure_assets USING GIST (geom);
