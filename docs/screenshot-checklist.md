# Screenshot Checklist — Sunrise Dental Clinic

**Date:** 2026-09-05  
**Purpose:** Guide for manual screenshot capture supporting academic assessment submissions (Task B, Task C, and Task D).

> [!NOTE]
> All screenshots are to be captured **MANUALLY** during browser testing and terminal execution. Do not fabricate screenshot evidence.

---

## Task B Screenshots — Application Functionality & Infrastructure

| Figure ID | Page / Tool | What Must Be Visible | Assessment Criterion | Suggested Figure Caption |
|---|---|---|---|---|
| **Fig-B01** | Browser — Landing Page | Landing page banner, clinic overview, login navigation link, responsive layout. | Core Web UI & Layout | *Figure B1: Sunrise Dental Clinic Landing Page* |
| **Fig-B02** | Browser — Login Page | Login form with username and password fields, submit button, CSRF hidden input. | FR-01 Authentication UI | *Figure B2: Authentication Login Interface* |
| **Fig-B03** | Browser — Dashboard | Dashboard header greeting logged-in user, today's appointments counter, quick action cards. | Dashboard Metrics & Session State | *Figure B3: Main Dashboard Overview* |
| **Fig-B04** | Browser — New Appointment Form | Appointment form showing patient fields, dentist dropdown, treatment selector, date/time pickers. | FR-02 Appointment Registration UI | *Figure B4: New Appointment Registration Form* |
| **Fig-B05** | Browser — Double Booking Error | Form validation error banner displaying dentist conflict message upon duplicate slot booking. | FR-02 Business Conflict Validation | *Figure B5: Dentist Schedule Double-Booking Rejection* |
| **Fig-B06** | Browser — Appointment Search | Search filters (reference number, patient name, status) and populated search result table. | FR-03 Search & List View | *Figure B6: Appointment Search & Filtering Results* |
| **Fig-B07** | Browser — Appointment Details | Full appointment detail page showing patient contact, dentist, treatment, and status badge. | FR-03 Detail Management View | *Figure B7: Appointment Record Details* |
| **Fig-B08** | Browser — Billing Calculation | Billing generation screen with treatment cost, consultation fee breakdown, and total computation. | FR-04 Bill Calculation UI | *Figure B8: Bill Generation & Fee Calculation* |
| **Fig-B09** | Browser — Receipt Page (Screen View) | Issued receipt card with bill number, appointment reference, charges table, and action buttons. | FR-05 Bill Receipt View | *Figure B9: On-Screen Bill Receipt* |
| **Fig-B10** | Browser — Receipt Print Preview | Print preview dialog showing clean formatted receipt with sidebar, header, and buttons hidden. | FR-05 Receipt Print Layout | *Figure B10: Receipt Print Preview Output* |
| **Fig-B11** | Browser — Reports Page | Daily schedule summary table, revenue accumulation chart/metrics, and popular treatments list. | FR-06 Analytical Reporting | *Figure B11: Clinic Daily Reports & Revenue Analytics* |
| **Fig-B12** | Browser — Help Page | Help documentation sections, operational guidelines, and FAQ. | FR-06 User Help Interface | *Figure B12: Online Help & Documentation Page* |
| **Fig-B13** | Database — Terminal / pgAdmin | Results of `SELECT * FROM appointments;` and `SELECT * FROM bills;` showing persisted data. | Database Persistence | *Figure B13: PostgreSQL Database Verification Queries* |
| **Fig-B14** | REST — cURL / Postman | HTTP request to `/api/health` returning HTTP 200 OK and `{"status":"UP"}` JSON response. | REST API Health Endpoint | *Figure B14: REST API Health Check Response* |
| **Fig-B15** | REST — cURL / Postman | HTTP request to `/api/appointments` returning JSON array of appointment entities. | REST API Collection Endpoint | *Figure B15: REST API Appointments Endpoint Response* |

---

## Task C Screenshots — Testing & Quality Evidence

| Figure ID | Page / Tool | What Must Be Visible | Assessment Criterion | Suggested Figure Caption |
|---|---|---|---|---|
| **Fig-C01** | Terminal — `mvn clean test` | Terminal execution output showing `Tests run: 106, Failures: 0, Errors: 0, Skipped: 0` and `BUILD SUCCESS`. | Automated Testing Evidence | *Figure C1: Maven Clean Test Suite Completion (106 Executions Passed)* |
| **Fig-C02** | Terminal — TDD Evidence (RED Phase) | Historical test log (`phase-2-tdd-red.txt`) showing intentional test failure prior to code implementation. | Test-Driven Development (TDD) | *Figure C2: TDD Red Phase — Double-Booking Conflict Failure* |
| **Fig-C03** | Terminal — TDD Evidence (GREEN Phase) | Historical test log (`phase-2-tdd-green.txt`) showing test pass following code implementation. | Test-Driven Development (TDD) | *Figure C3: TDD Green Phase — Double-Booking Conflict Passing* |
| **Fig-C04** | Document — Test Plan | Screenshot of `docs/test-plan.md` showing test strategy and execution summary table. | Test Documentation | *Figure C4: Master Test Plan & Execution Matrix* |

---

## Task D Screenshots — Repository & Build Artifact Evidence

| Figure ID | Page / Tool | What Must Be Visible | Assessment Criterion | Suggested Figure Caption |
|---|---|---|---|---|
| **Fig-D01** | Terminal — `git status` / `git log` | Terminal output showing `git status -sb` in sync with `origin/main` and recent structured commit messages. | Git Version Control Integrity | *Figure D1: Git Log & Branch Synchronization Status* |
| **Fig-D02** | Web — GitHub Repository | Browser view of GitHub project page (`HammadAhmedAR/sunshine-dental-hmd`) showing main branch commits. | Remote Repository Deployment | *Figure D2: GitHub Remote Repository Overview* |
| **Fig-D03** | Terminal — Maven Packaging | Terminal output of `mvn clean package` showing successful WAR creation. | Artifact Packaging | *Figure D3: Maven Clean Package WAR Generation* |
| **Fig-D04** | File Explorer / Terminal | Existence and size of `target/sunrise-dental-clinic.war`. | Target WAR Artifact | *Figure D4: Compiled WAR Deployment Artifact* |
