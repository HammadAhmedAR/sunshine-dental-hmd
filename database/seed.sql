-- Fictional development data only. Prices are illustrative, not clinical quotes.
-- Safe to rerun: conflicts on stable natural keys leave existing data untouched.
BEGIN;
-- Disabled until a later authentication milestone provisions a real salted hash.
-- NULL means unprovisioned; there is no default/shared plaintext password.
INSERT INTO users (username, display_name, password_hash, role, is_active)
VALUES ('sunrise.admin', 'Sunrise Administrator', NULL, 'ADMIN', FALSE)
ON CONFLICT (username) DO NOTHING;

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
