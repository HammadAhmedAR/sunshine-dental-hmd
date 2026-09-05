# Manual Runtime Verification Guide — Sunrise Dental Clinic

**Date:** 2026-09-05  
**Target Environment:** PostgreSQL 15, GlassFish 7.0.x (Jakarta EE 10), Microsoft Edge / Google Chrome  
**Application Context Root:** `/sunrise-dental-clinic`  
**WAR File Location:** `target/sunrise-dental-clinic.war`  

---

## 1. Database Setup & Migration Procedure (PostgreSQL)

Execute the following commands in order using `psql` or pgAdmin against your PostgreSQL instance.

### A. Database Creation
```sql
CREATE DATABASE sunrisedb;
```

### B. Migration Execution Order
Apply files in exact numerical order:

```bash
# 1. Base Schema
psql -U postgres -d sunrisedb -f database/schema.sql

# 2. Seed Reference Data
psql -U postgres -d sunrisedb -f database/seed.sql

# 3. Patient Address & Booking Guard Migration
psql -U postgres -d sunrisedb -f database/migrations/002_patient_address_and_booking_guard.sql

# 4. Appointment Reference Format Migration
psql -U postgres -d sunrisedb -f database/migrations/003_appointment_reference_format.sql

# 5. Billing Snapshots & Settings Migration
psql -U postgres -d sunrisedb -f database/migrations/004_billing_snapshots_and_settings.sql

# 6. Billing Revenue Analytical View Migration
psql -U postgres -d sunrisedb -f database/migrations/005_billing_revenue_view.sql
```

### C. Verification Queries
After running all migrations, execute:
```sql
-- Verify table structure
SELECT table_name FROM information_schema.tables WHERE table_schema='public';

-- Verify seeded user
SELECT id, username, full_name, role, active FROM users;

-- Verify seeded dentists and treatments
SELECT count(*) FROM dentists;
SELECT count(*) FROM treatments;

-- Verify reference sequence
SELECT last_value FROM appointment_number_seq;
```

---

## 2. Application Server Setup (GlassFish 7)

### A. Start Domain
```bash
asadmin start-domain domain1
```

### B. Deploy WAR Package
```bash
asadmin deploy --contextroot /sunrise-dental-clinic target/sunrise-dental-clinic.war
```

### C. Redeploy After Changes
```bash
asadmin deploy --force=true --contextroot /sunrise-dental-clinic target/sunrise-dental-clinic.war
```

### D. Undeploy Application
```bash
asadmin undeploy sunrise-dental-clinic
```

---

## 3. Browser Verification Map

Access the application at `http://localhost:8080/sunrise-dental-clinic`.

| Page | URL | Expected Behavior |
|---|---|---|
| **Landing Page** | `http://localhost:8080/sunrise-dental-clinic/` | Renders public landing page with hero banner, clinic overview, and Login action link. |
| **Login Page** | `http://localhost:8080/sunrise-dental-clinic/login` | Renders credential form (Username & Password) with CSRF protection token. |
| **Dashboard** | `http://localhost:8080/sunrise-dental-clinic/dashboard` | Displays summary statistics (today's appointments, upcoming, active dentists), quick links, user greeting. |
| **New Appointment** | `http://localhost:8080/sunrise-dental-clinic/appointments/new` | Renders registration form with patient details, dentist selection, treatment dropdown, date/time pickers. |
| **Appointment Search**| `http://localhost:8080/sunrise-dental-clinic/appointments/search` | Search form accepting reference number, patient name, contact number, or status filters. |
| **Appointment Details**| `http://localhost:8080/sunrise-dental-clinic/appointments/details?id=<APT_ID>` | Detailed record view showing patient, dentist, treatment, scheduled time, and status. |
| **Edit / Reschedule** | `http://localhost:8080/sunrise-dental-clinic/appointments/edit?id=<APT_ID>` | Form to modify appointment date/time or dentist assignment. |
| **Billing Generation**| `http://localhost:8080/sunrise-dental-clinic/billing/generate?appointmentId=<APT_ID>` | Invoicing form showing breakdown of treatment cost + consultation fee. |
| **Billing History** | `http://localhost:8080/sunrise-dental-clinic/billing` | Table of issued bills with bill number, appointment reference, total amount, and date. |
| **Bill / Receipt** | `http://localhost:8080/sunrise-dental-clinic/billing/receipt?id=<BILL_ID>` | Formal bill receipt view styled for printing. |
| **Reports** | `http://localhost:8080/sunrise-dental-clinic/reports` | Analytical reporting suite for daily schedules, revenue, and popular treatments. |
| **Help Page** | `http://localhost:8080/sunrise-dental-clinic/help` | System documentation and operational guidance. |
| **Logout** | `POST http://localhost:8080/sunrise-dental-clinic/logout` | Invalidates session and redirects to `/login?logout=true`. |

---

## 4. REST API Verification Procedures

### A. Health Endpoint
```bash
curl -i http://localhost:8080/sunrise-dental-clinic/api/health
```
- **Expected Status:** `HTTP/1.1 200 OK`
- **Content-Type:** `application/json`
- **Response Payload:** `{"status":"UP","timestamp":"..."}`

### B. Appointments Collection Endpoint
```bash
curl -i http://localhost:8080/sunrise-dental-clinic/api/appointments
```
- **Expected Status:** `HTTP/1.1 200 OK`
- **Content-Type:** `application/json`
- **Response Payload:** Array of appointment JSON objects `[{"appointmentNumber":"APT-2026-00001", ...}]`

---

## 5. Manual Business Test Cases

### Test Case 1: Valid Login
1. Navigate to `/login`.
2. Input `sunrise.admin` / `SunriseLocal!2026`. Submit.
3. **Pass Criterion:** Redirected to `/dashboard` with session cookie set.

### Test Case 2: Invalid Login
1. Navigate to `/login`.
2. Input `sunrise.admin` / `WrongPassword`. Submit.
3. **Pass Criterion:** Form re-renders with message `"Invalid username or password"`.

### Test Case 3: Register Appointment
1. Navigate to `/appointments/new`.
2. Fill patient name "Kavinda Perera", contact `0771234567`, address "123 Galle Road, Colombo 03".
3. Select Dentist, Treatment, future Date, valid Time. Submit.
4. **Pass Criterion:** Success message displays generated appointment number `APT-2026-XXXXX`.

### Test Case 4: Rejection of Past Appointment Date
1. Navigate to `/appointments/new`.
2. Select past date (e.g. `2020-01-01`). Submit.
3. **Pass Criterion:** Form re-renders with error `"Appointment date cannot be in the past"`.

### Test Case 5: Dentist Double-Booking Prevention
1. Register appointment for Dentist A at `10:00 AM` on Date D.
2. Attempt to register another appointment for Dentist A at `10:00 AM` on Date D.
3. **Pass Criterion:** Error displays `"Selected dentist is already booked for this time slot"`.

### Test Case 6: Appointment Search
1. Navigate to `/appointments/search`.
2. Enter appointment reference or patient name. Click Search.
3. **Pass Criterion:** Matching appointments displayed in search result table.

### Test Case 7: Appointment Rescheduling
1. Open appointment details and click "Reschedule".
2. Change time slot to an open slot. Submit.
3. **Pass Criterion:** Appointment status updated and new time reflected in details.

### Test Case 8: Appointment Cancellation
1. Open appointment details, select Status -> `CANCELLED`. Submit.
2. **Pass Criterion:** Status updates to `CANCELLED`, releasing dentist time slot.

### Test Case 9: Calculate and Generate Bill
1. Navigate to `/billing/generate?appointmentId=<ID>`.
2. Verify treatment fee and consultation fee summation. Submit.
3. **Pass Criterion:** Bill created, redirect to `/billing/receipt?id=<BILL_ID>`.

### Test Case 10: Duplicate Billing Prevention
1. Attempt to access `/billing/generate?appointmentId=<ID>` for an already billed appointment.
2. **Pass Criterion:** System rejects generation with error `"Bill has already been generated for this appointment"`.

### Test Case 11: Protected Route Access Enforcement
1. Clear browser cookies / logout.
2. Attempt to directly access `http://localhost:8080/sunrise-dental-clinic/dashboard`.
3. **Pass Criterion:** Automatically redirected to `/login`.

---

## 6. Manual Receipt Print Preview Verification

1. Navigate to an issued receipt: `/billing/receipt?id=1`.
2. Click the **Print receipt** button or press `Ctrl + P`.
3. Review the browser's Print Preview modal.

### Print-Visible Content (Must Appear)
- Header: **Sunrise Dental Clinic**
- Title: **Bill / Receipt**
- Bill Number (e.g. `BIL-2026-00001`)
- Appointment Reference (e.g. `APT-2026-00001`)
- Patient Name
- Dentist Name
- Treatment Name
- Itemized charges: Treatment Cost & Consultation Fee
- Total Amount (LKR)
- Issued Date & Sri Lanka Time

### Hidden Content (Must BE Hidden)
- Sidebar navigation menu
- Topbar user profile / header
- "Print receipt" action button
- "Billing history" link
- Flash alert banner / status messages

**Status:** `PENDING MANUAL VERIFICATION`
