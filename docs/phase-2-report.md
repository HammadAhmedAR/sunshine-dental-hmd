# Phase 2 completion report

This is working development evidence, not the final assignment report. The completed scope is login, session protection, dashboard, clinic reference data and appointment registration. Earlier implementation details and runtime evidence are preserved in [phase-2-development.md](phase-2-development.md); this report supersedes its reference format, test count and remote-status statements.

## Files and dependencies

The [complete project tree](directory-tree.txt) and [added/modified/deleted file inventory](phase-2-changes.txt) cover all changes since Phase 1 commit 343704c. The latest revision adds `AppointmentNumberGenerator`, its seven tests, `JdbcAppointmentDAOTest`, migration 003 and `Phase2ReferenceCheck`. It updates AuthService, JdbcAppointmentDAO, the appointment view, verification scripts and evidence documents.

Phase 2's concrete classes include LoginServlet, LogoutServlet, AuthenticationFilter, AuthService, User/UserDAO/JdbcUserDAO, DashboardServlet/DashboardService/DashboardDAO/DashboardStats, Patient/PatientDraft/PatientService/PatientDAO/JdbcPatientDAO, Dentist/DentistService/DentistDAO/JdbcDentistDAO, Treatment/TreatmentService/TreatmentDAO/JdbcTreatmentDAO, Appointment/AppointmentRequest/AppointmentService/AppointmentDAO/JdbcAppointmentDAO, AddAppointmentServlet, BookingConflictException and ValidationException. Services wires dependencies explicitly; ConnectionProvider, DBConnection, Csrf, PasswordHasher and SessionConfiguration provide focused infrastructure.

| Dependency added during Phase 2 | Version | Scope |
| --- | --- | --- |
| Bouncy Castle bcprov-jdk18on | 1.83 | Packaged BCrypt implementation |
| jakarta.servlet.jsp.jstl-api | 3.0.0 | Packaged |
| org.glassfish.web:jakarta.servlet.jsp.jstl | 3.0.1 | Packaged |
| jakarta.el-api | 5.0.0 | Provided by GlassFish |
| mockito-core | 5.14.2 | Test only |

Java 17, Servlet 6, REST 3.1, PostgreSQL JDBC, JUnit 5 and Maven WAR packaging are retained. No new dependency was needed for the reference-format revision. MVC, DAO and Service Layer are implemented; constructor injection supports tests without a framework. SQL remains in DAOs, apart from schema and explicit verification scripts.

## Authentication and sessions

GET `/login` renders a WEB-INF JSP with accessible fields and a CSRF token. POST passes credentials through LoginServlet -> AuthService -> UserDAO -> PostgreSQL. Blank usernames/passwords are rejected before database access. BCrypt verifies the stored hash; invalid credentials produce one generic message and inactive accounts cannot sign in.

Successful authentication invalidates the previous session, creates a new session, stores a safe User identity without credentials and redirects to `/dashboard`. Logout is a CSRF-protected POST that invalidates the session. The filter protects all business routes centrally; landing/login/assets and `/api/health` remain public. Sessions expire after 20 minutes and use HttpOnly, SameSite=Lax, cookie-only tracking. HTTPS deployments must enable secure cookies as described in README.

The local-only seeded account remains `sunrise.admin` / `SunriseLocal!2026`. PostgreSQL stores its BCrypt hash, never the plaintext password. The example account names in the request were illustrative; no duplicate administrator account was added.

## Dashboard and clinic data

DashboardServlet -> DashboardService -> DashboardDAO produces live today's/upcoming appointment counts with Asia/Colombo day boundaries. The JSP greets the logged-in user's full name. PatientService lists existing patients and validates new details. DentistService and TreatmentService obtain active database rows; treatment fees use BigDecimal and durations come from the database. No dentist names or treatment fees are hardcoded in controllers or JSPs.

## Appointment registration and transaction

The form posts to AddAppointmentServlet -> AppointmentService -> DAOs. Raw form strings are validated and parsed in the service; persisted start/end times use Instant, derived from LocalDate/LocalTime in Sri Lankan time. Records store normalized foreign keys rather than duplicated patient/dentist/treatment data.

The service opens one JDBC connection, sets READ_COMMITTED and autoCommit(false), locks the selected dentist, validates active reference rows and checks overlap through AppointmentDAO. It creates a new patient or reuses a selected patient, inserts the appointment and commits. SQL or runtime failures trigger rollback; neither insert is left partially saved. The database's dentist/start unique index independently guards identical active slots. The DAO supplies lookup results; the service decides whether booking is allowed.

Success uses Post/Redirect/Get and displays the generated appointment number. Validation errors retain submitted values and display a friendly message; database errors return a generic response and log SQL state server-side without exposing stack traces or credentials to users.

## Validation and schema changes

- Required patient name: at least two letters, maximum 120 characters, supported Unicode name characters.
- Required address: trimmed length 3–300.
- Required Sri Lankan contact: local 0 or international +94 format; new patient numbers normalize to +94.
- Required valid positive dentist/treatment IDs resolving to active records.
- Required real date and whole-minute time; no past appointment; year 2000–9999.
- Required valid selected patient when reusing a record; incomplete legacy patient details must be backfilled.
- Same dentist/start or overlapping duration rejected; adjacent slots allowed.
- Appointment reference uniqueness remains enforced by PostgreSQL.

Schema changes across Phase 2 are patient address/contact constraints, active dentist/start uniqueness and BCrypt seed provisioning. The latest migration `003_appointment_reference_format.sql` permits new `APT-YYYY-NNNNN` references while preserving existing SDC references and sequence state. It removes the old SDC default. The corresponding fresh schema is updated. Existing Phase 1 databases apply 002 then 003; databases already on the earlier Phase 2 schema apply only 003. Fresh installations use schema.sql and neither migration. No records are renumbered or tables dropped.

## Tests and genuine TDD

| Test class | Passing executions |
| --- | ---: |
| DatabaseConfigTest | 9 |
| SessionServletTest | 2 |
| AuthenticationFilterTest | 6 |
| AuthServiceTest | 14 |
| PatientServiceTest | 12 |
| DashboardServiceTest | 1 |
| AppointmentServiceTest | 19 |
| JdbcAppointmentDAOTest | 1 |
| AppointmentNumberGeneratorTest | 7 |
| Total | 71 |

This is 62 more executions than Phase 1, including 16 added by the latest revision. Tests cover credentials, patient rules, clock/time-zone boundaries, active references, double booking, rollback, patient reuse, session rotation, CSRF, reference format/nonblank/year/sequence behavior and DAO binding across the Sri Lankan New Year boundary. They do not need a running database or GlassFish.

The original double-booking RED checkpoint remains commit 38df028: one assertion failure, followed by its recorded GREEN result. It was not manufactured again. The latest genuine checkpoint is c7254fe: four blank-password cases ran with two failures because whitespace-only input reached the DAO. After the rule was added, all four passed; the same targeted run also passed seven reference-generator tests. Raw evidence remains under docs/evidence, with chronology in test-plan.md.

## Build and runtime evidence

`mvn -B clean test` and `mvn -B clean package` both ended in BUILD SUCCESS: 71 tests, zero failures, errors or skips. `target/sunrise-dental-clinic.war` exists (11,979,288 bytes when verified). Current build logs are `phase-2-revision-clean-test.txt` and `phase-2-revision-clean-package.txt`; earlier logs are retained as historical evidence.

Earlier Phase 2 verification passed 19 HTTP checks, nine database/transaction/concurrency checks, a legacy-patient migration check and desktop/mobile browser inspection on isolated GlassFish 7.1.0/PostgreSQL 15. For this revision, migration 003 succeeded against that isolated PostgreSQL fixture; a real service/DAO registration saved `APT-2026-00007`, and existing SDC reference counts remained unchanged. See `phase-2-reference-migration.txt` and `phase-2-reference-check.txt`.

The revised WAR has not been redeployed to the user's normal domain, and the complete HTTP/browser suite was not repeated for the reference-only UI wording change. Actual deployment/migration, HTTPS/secure-cookie configuration, full idle timeout and assessment screenshot capture remain manual. No screenshots or runtime results have been fabricated.

## Git history and publication

Phase 1 history is retained. The main Phase 2 commits are eb35a13 (authentication), 591da47 (dashboard/reference data), 38df028 (double-booking RED), 39159cb (registration/GREEN/runtime evidence) and bc4632d (initial evidence documents). The existing repository initialization was merged in e4d292c. The revised tests are c7254fe, and the revised implementation was committed as d76acf1 (`core features`) before this continuation resumed.

The connected destination is `https://github.com/HammadAhmedAR/sunshine-dental-hmd.git`. A read-only `git ls-remote` check confirmed remote main at d76acf19690eedcb05f403959c0f9ebf4ed529ea, matching the code commit. The final report/inventory commit is created after this report; publication of that documentation is reported separately in the completion response. No force push or history rewrite is used.

## Remaining scope and limitations

Not implemented: appointment search/details, editing/rescheduling, billing, reports, help, advanced REST endpoints, CI/CD, final UML or final assignment documentation. No Phase 3 work has started.

Other limits remain account administration/password reset/rate limiting, role-specific authorisation, patient-list pagination, opening-hours/holiday rules and broader accessibility/load testing. Different-start overlaps are serialized through the service's dentist lock; arbitrary external SQL writers must use the same protocol. The local demo account must not be used as a public deployment credential.
