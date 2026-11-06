USE crew_management;

-- Existing flights cannot be assigned until these values are populated with
-- verified IANA ZoneIds for their source and destination airports.
ALTER TABLE flight
    ADD COLUMN source_zone_id VARCHAR(64) NULL,
    ADD COLUMN destination_zone_id VARCHAR(64) NULL;

-- Example only; replace with verified values for each existing flight:
-- UPDATE flight SET source_zone_id='Asia/Kolkata', destination_zone_id='Europe/London' WHERE flight_id=1;

-- Required configuration for routes crossing time zones:
-- Start the JVM with -Dcrew.rest.multipleZoneMinimumHours=<DGCA-approved value, 12..36>.
-- Until a value is configured, multi-time-zone assignments are rejected safely.
