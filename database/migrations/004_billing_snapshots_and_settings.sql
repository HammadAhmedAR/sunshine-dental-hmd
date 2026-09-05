-- Upgrade after 003. Keeps existing bills and their totals intact.
BEGIN;
CREATE TABLE clinic_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    monetary_value NUMERIC(12,2) NOT NULL CHECK (monetary_value >= 0 AND monetary_value <> 'NaN'::NUMERIC)
);
-- Illustrative local consultation fee in LKR; maintained centrally by a database administrator.
INSERT INTO clinic_settings VALUES ('consultation_fee', 500.00);

CREATE SEQUENCE bill_reference_seq;
ALTER TABLE bills
    ADD COLUMN bill_number VARCHAR(40),
    ADD COLUMN treatment_cost NUMERIC(12,2),
    ADD COLUMN consultation_fee NUMERIC(12,2),
    ADD COLUMN patient_name VARCHAR(120),
    ADD COLUMN dentist_name VARCHAR(120),
    ADD COLUMN treatment_name VARCHAR(100);
-- Historical component breakdown was not stored. Preserve subtotal as treatment cost,
-- zero consultation, and the original discount/total. Snapshot current labels at migration.
UPDATE bills b SET bill_number = 'BILL-LEGACY-' || b.bill_id,
    treatment_cost=b.subtotal, consultation_fee=0,
    patient_name=p.full_name, dentist_name=d.full_name, treatment_name=t.name
FROM appointments a JOIN patients p ON p.patient_id=a.patient_id
JOIN dentists d ON d.dentist_id=a.dentist_id JOIN treatments t ON t.treatment_id=a.treatment_id
WHERE b.appointment_id=a.appointment_id;
ALTER TABLE bills
    ALTER COLUMN bill_number SET NOT NULL,
    ALTER COLUMN treatment_cost SET NOT NULL,
    ALTER COLUMN consultation_fee SET NOT NULL,
    ALTER COLUMN patient_name SET NOT NULL,
    ALTER COLUMN dentist_name SET NOT NULL,
    ALTER COLUMN treatment_name SET NOT NULL,
    ADD CONSTRAINT bills_bill_number_key UNIQUE (bill_number),
    ADD CONSTRAINT bills_bill_number_format CHECK (bill_number ~ '^BILL-([0-9]{4}-[0-9]{5,}|LEGACY-[0-9]+)$'),
    ADD CONSTRAINT bills_cost_valid CHECK (treatment_cost >= 0 AND treatment_cost <> 'NaN'::NUMERIC),
    ADD CONSTRAINT bills_consultation_valid CHECK (consultation_fee >= 0 AND consultation_fee <> 'NaN'::NUMERIC),
    ADD CONSTRAINT bills_components_match CHECK (subtotal = treatment_cost + consultation_fee);
ALTER SEQUENCE bill_reference_seq OWNED BY bills.bill_number;
-- History ordering and billing-date report range scans.
CREATE INDEX idx_bills_issued_at ON bills(issued_at, bill_id);
-- List and daily schedule use a half-open starts_at range; reference already has a UNIQUE index.
CREATE INDEX idx_appointments_start ON appointments(starts_at, appointment_id);
COMMIT;
