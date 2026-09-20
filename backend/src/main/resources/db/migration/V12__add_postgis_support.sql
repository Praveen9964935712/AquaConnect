CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE incidents
    ADD COLUMN IF NOT EXISTS geom geometry(Point, 4326);

UPDATE incidents
SET geom = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL
  AND geom IS NULL;

CREATE INDEX IF NOT EXISTS idx_incidents_geom
    ON incidents USING GIST (geom);
