# Requirements Traceability Matrix — Sunrise Dental Clinic

**Date:** 2026-09-05  
**Version:** 1.0.0  
**Project:** Sunrise Dental Clinic Management System  

This document maps all functional requirements (FR-01 to FR-07) across implementation components, automated tests, database entities, manual verification procedures, and assessment evidence placeholders.

---

## Traceability Matrix

| Requirement ID & Description | Implementation Source Files | Automated Test Suite | Database Artifacts | Manual Verification Procedure | Screenshot Placeholder | Requirement Status |
|---|---|---|---|---|---|---|
| **FR-01 User Authentication**<br>Allow authorized staff to log in securely with username and password. | - `LoginServlet.java`<br>- `AuthService.java`<br>- `JdbcUserDAO.java`<br>- `User.java`<br>- `login.jsp` | - `AuthServiceTest.java` (14)<br>- `AuthenticationFilterTest.java` (7)<br>- `SessionServletTest.java` (2) | - `users` table<br>- Index `idx_users_username` | Manual Test Case 1 & 2 in `manual-runtime-verification.md` | Fig-B02 | MANUAL VERIFICATION PENDING |
| **FR-02 Register New Appointment**<br>Register patient and appointment details with dentist availability validation. | - `AddAppointmentServlet.java`<br>- `AppointmentService.java`<br>- `PatientService.java`<br>- `DentistService.java`<br>- `TreatmentService.java`<br>- `JdbcAppointmentDAO.java`<br>- `add-appointment.jsp` | - `AppointmentServiceTest.java` (19)<br>- `PatientServiceTest.java` (12)<br>- `JdbcAppointmentDAOTest.java` (1)<br>- `AppointmentNumberGeneratorTest.java` (7) | - `appointments`<br>- `patients`<br>- `dentists`<br>- `treatments`<br>- Index `idx_appointments_dentist_start_active`<br>- Sequence `appointment_number_seq` | Manual Test Case 3, 4, 5 in `manual-runtime-verification.md` | Fig-B04, Fig-B05 | MANUAL VERIFICATION PENDING |
| **FR-03 Search / Display Appointment Details**<br>Search appointments by reference, patient name, contact, status, or date; view, reschedule, or cancel. | - `AppointmentSearchServlet.java`<br>- `AppointmentDetailsServlet.java`<br>- `AppointmentEditServlet.java`<br>- `AppointmentStatusServlet.java`<br>- `AppointmentQueryService.java`<br>- `AppointmentManagementService.java`<br>- `list.jsp`, `details.jsp`, `edit.jsp` | - `AppointmentQueryServiceTest.java` (6)<br>- `AppointmentManagementServiceTest.java` (9) | - `appointments` table<br>- Indexes on `appointment_number`, `patient_name`, `start_time`, `status` | Manual Test Case 6, 7, 8 in `manual-runtime-verification.md` | Fig-B06, Fig-B07 | MANUAL VERIFICATION PENDING |
| **FR-04 Calculate Bill**<br>Calculate treatment cost plus clinic consultation fee using exact currency arithmetic. | - `BillingServlet.java`<br>- `BillService.java`<br>- `JdbcBillDAO.java`<br>- `Bill.java`<br>- `generate.jsp`, `history.jsp` | - `BillServiceTest.java` (11) | - `bills` table<br>- `clinic_settings` table<br>- Constraint `unique_appointment_bill` | Manual Test Case 9 & 10 in `manual-runtime-verification.md` | Fig-B08 | MANUAL VERIFICATION PENDING |
| **FR-05 Print Bill**<br>Display formal bill receipt formatted for clean A4 print preview. | - `BillingServlet.java`<br>- `BillService.java`<br>- `receipt.jsp`<br>- `receipt.css` (`@media print`) | - `BillServiceTest.java` (11) | - `bills` table<br>- `appointments` table<br>- `patients` table | Manual Print Test in `manual-runtime-verification.md` | Fig-B09, Fig-B10 | MANUAL VERIFICATION PENDING (VISUAL REVIEW: MANUAL, PRINT PREVIEW: MANUAL) |
| **FR-06 Help**<br>Provide operational guidance and system instructions for clinic staff. | - `HelpServlet.java`<br>- `help.jsp` | - `AuthenticationFilterTest.java` (route protection test) | N/A | Navigation to `/help` URL | Fig-B12 | MANUAL VERIFICATION PENDING |
| **FR-07 Safe Logout**<br>Safely invalidate user session and clear authentication context. | - `LogoutServlet.java`<br>- `AuthService.java` | - `SessionServletTest.java` (2)<br>- `AuthenticationFilterTest.java` (1) | N/A | Manual Test Case 11 in `manual-runtime-verification.md` | Fig-B02 | MANUAL VERIFICATION PENDING |

---

## Coverage Verification Summary

- **Total Functional Requirements:** 7 (FR-01 to FR-07)
- **Implemented Code Components:** 100% (Controllers, Services, DAOs, Models, Views present for all requirements)
- **Automated Test Coverage:** 106 unit/integration tests covering all business services, controllers, DAOs, and configuration logic.
- **Database Entity Alignment:** Schema tables, foreign keys, unique constraints, and sequence generators fully match domain models.
- **Manual Verification Status:** All requirements are set to `MANUAL VERIFICATION PENDING` pending manual GlassFish deployment and browser validation.
