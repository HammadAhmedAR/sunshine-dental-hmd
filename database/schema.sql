-- Run once against an empty sunrise_dental_clinic database.
-- DDL is transactional; existing tables are deliberately not dropped.
BEGIN;

CREATE TABLE users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE CHECK (username ~ '^[a-z][a-z0-9_.]{2,49}$'),
    display_name VARCHAR(100) NOT NULL CHECK (btrim(display_name) <> ''),
    password_hash VARCHAR(255),
    role VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT active_user_has_hash CHECK (
        NOT is_active OR (password_hash IS NOT NULL AND btrim(password_hash) <> '')
    )
);

CREATE TABLE patients (
    patient_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL CHECK (btrim(full_name) <> ''),
    address VARCHAR(300) NOT NULL CONSTRAINT patients_address_required CHECK (char_length(btrim(address)) BETWEEN 3 AND 300),
    phone VARCHAR(16) NOT NULL CONSTRAINT patients_sri_lankan_phone CHECK (phone ~ '^(0|\+94)[1-9][0-9]{8}$'),
    email VARCHAR(254) CHECK (email IS NULL OR btrim(email) <> ''),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dentists (
    dentist_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    registration_number VARCHAR(30) NOT NULL UNIQUE CHECK (btrim(registration_number) <> ''),
    full_name VARCHAR(120) NOT NULL CHECK (btrim(full_name) <> ''),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE treatments (
    treatment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    treatment_code VARCHAR(20) NOT NULL UNIQUE CHECK (btrim(treatment_code) <> ''),
    name VARCHAR(100) NOT NULL UNIQUE CHECK (btrim(name) <> ''),
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0 AND price <> 'NaN'::NUMERIC),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 5 AND 480),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Separate server-side sequence avoids MAX(id)+1 and client-supplied references.
CREATE SEQUENCE appointment_reference_seq;
CREATE TABLE appointments (
    appointment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appointment_number VARCHAR(32) NOT NULL UNIQUE
        CHECK (appointment_number ~ '^(SDC-[0-9]+|APT-[0-9]{4}-[0-9]{5,})$'),
    patient_id BIGINT NOT NULL REFERENCES patients(patient_id),
    dentist_id BIGINT NOT NULL REFERENCES dentists(dentist_id),
    treatment_id BIGINT NOT NULL REFERENCES treatments(treatment_id),
    created_by BIGINT NOT NULL REFERENCES users(user_id),
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'BOOKED'
        CHECK (status IN ('BOOKED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (ends_at > starts_at)
);
ALTER SEQUENCE appointment_reference_seq OWNED BY appointments.appointment_number;
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_dentist_time ON appointments(dentist_id, starts_at);
CREATE INDEX idx_appointments_treatment ON appointments(treatment_id);
CREATE INDEX idx_appointments_creator ON appointments(created_by);
CREATE UNIQUE INDEX uq_appointments_dentist_start
    ON appointments(dentist_id, starts_at) WHERE status IN ('BOOKED', 'COMPLETED');

CREATE TABLE bills (
    bill_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(appointment_id),
    issued_by BIGINT NOT NULL REFERENCES users(user_id),
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0 AND subtotal <> 'NaN'::NUMERIC),
    discount NUMERIC(12,2) NOT NULL DEFAULT 0
        CHECK (discount >= 0 AND discount <= subtotal AND discount <> 'NaN'::NUMERIC),
    total NUMERIC(12,2) GENERATED ALWAYS AS (subtotal - discount) STORED,
    currency CHAR(3) NOT NULL DEFAULT 'LKR' CHECK (currency = 'LKR'),
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_bills_issuer ON bills(issued_by);

-- Upgrade after 003. Keeps existing bills and their totals intact.

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


-- Report source shared by billing summaries. Issued revenue is not payment collection.

CREATE VIEW billing_revenue_summary AS
SELECT (issued_at AT TIME ZONE 'Asia/Colombo')::DATE AS revenue_date,
       count(*) AS bill_count,
       sum(treatment_cost) AS treatment_revenue,
       sum(consultation_fee) AS consultation_revenue,
       sum(discount) AS discounts,
       sum(total) AS total_revenue
FROM bills GROUP BY (issued_at AT TIME ZONE 'Asia/Colombo')::DATE;
-- Supports the report view's local-date predicate; issued_at index separately serves ordered history.
CREATE INDEX idx_bills_local_date ON bills (((issued_at AT TIME ZONE 'Asia/Colombo')::DATE));


COMMIT;
