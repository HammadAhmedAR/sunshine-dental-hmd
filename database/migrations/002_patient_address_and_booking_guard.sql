-- Existing Phase 1 databases only. Run once with psql -v ON_ERROR_STOP=1.
-- Fresh installations use schema.sql instead; never run both on the same database.
BEGIN;
ALTER TABLE patients ADD COLUMN address VARCHAR(300);
-- Legacy patients are retained. New/updated rows must supply a real address.
-- Backfill legacy rows with verified addresses, then VALIDATE this constraint.
ALTER TABLE patients ADD CONSTRAINT patients_address_required
    CHECK (address IS NOT NULL AND char_length(btrim(address)) BETWEEN 3 AND 300) NOT VALID;
ALTER TABLE patients ADD CONSTRAINT patients_sri_lankan_phone
    CHECK (phone ~ '^(0|\+94)[1-9][0-9]{8}$') NOT VALID;
-- Abort atomically if existing active duplicate slots require manual reconciliation.
CREATE UNIQUE INDEX uq_appointments_dentist_start
    ON appointments(dentist_id, starts_at) WHERE status IN ('BOOKED', 'COMPLETED');
COMMIT;
