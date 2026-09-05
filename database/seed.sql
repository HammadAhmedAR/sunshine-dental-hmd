-- Fictional development data only. Prices are illustrative, not clinical quotes.
-- Safe to rerun: conflicts on stable natural keys leave existing data untouched.
BEGIN;
-- LOCAL DEVELOPMENT ONLY. Password is documented in README, never stored in the DB.
-- Existing provisioned accounts are not reset when this script is rerun.
INSERT INTO users (username, display_name, password_hash, role, is_active)
VALUES ('sunrise.admin', 'Sunrise Administrator', '$2b$12$9TIJJGbuNDLMGFVbqSccI..NtN.Yfalx67KJfGfslFQoC/wXSE1g.', 'ADMIN', TRUE)
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash, is_active = TRUE
WHERE users.password_hash IS NULL AND users.is_active = FALSE;

INSERT INTO dentists (registration_number, full_name) VALUES
    ('DEMO-DENT-001', 'Dr. Nadeesha Perera'),
    ('DEMO-DENT-002', 'Dr. Arjun Silva')
ON CONFLICT (registration_number) DO NOTHING;

INSERT INTO treatments (treatment_code, name, price, duration_minutes) VALUES
    ('CONSULT', 'Dental consultation', 2000.00, 20),
    ('SCALE', 'Scaling and polishing', 6500.00, 45),
    ('FILL', 'Tooth-coloured filling', 8000.00, 45),
    ('EXTRACT', 'Simple extraction', 7500.00, 40),
    ('ROOT', 'Root canal treatment', 25000.00, 90)
ON CONFLICT (treatment_code) DO NOTHING;
COMMIT;
