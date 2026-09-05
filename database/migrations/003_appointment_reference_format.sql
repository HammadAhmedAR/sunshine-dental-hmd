-- Existing Phase 1/2 databases, after migration 002. Run once before deploying the revised WAR.
-- Preserve existing references, uniqueness and sequence state; no records are renumbered.
BEGIN;
ALTER TABLE appointments ALTER COLUMN appointment_number DROP DEFAULT;
ALTER TABLE appointments DROP CONSTRAINT appointments_appointment_number_check;
ALTER TABLE appointments ADD CONSTRAINT appointments_appointment_number_check
    CHECK (appointment_number ~ '^(SDC-[0-9]+|APT-[0-9]{4}-[0-9]{5,})$');
COMMIT;
