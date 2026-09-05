# Phase 2 development evidence

## Outcome and boundary

Implemented secure authentication foundation, sessions, dashboard statistics, patient/dentist/treatment reference data and appointment registration. Work stops at Phase 2. The user's Phase 1 runtime verification is accepted as prior context; the Phase 2 checks below were observed independently in this session.

## Implemented classes and views

| Area | Files/classes and responsibility |
| --- | --- |
| Composition/configuration | `Services` explicitly wires dependencies; `SessionConfiguration` sets cookie attributes; existing `DatabaseConfig` loads external configuration |
| Authentication | `User` is a safe session identity; `UserDAO` and `JdbcUserDAO` load account credentials; `PasswordHasher` wraps BCrypt; `AuthService` validates credentials and active status |
| HTTP security | `AuthenticationFilter` protects business routes and checks CSRF; `Csrf` creates random session tokens; `LoginServlet` rotates sessions; `LogoutServlet` invalidates sessions |
| Patient data | `Patient`, `PatientDraft`, `PatientDAO`, `JdbcPatientDAO`, `PatientService` handle listing, lookup, insertion and validation |
| Dentist data | `Dentist`, `DentistDAO`, `JdbcDentistDAO`, `DentistService` retrieve active dentists and lock the chosen dentist for registration |
| Treatment data | `Treatment`, `TreatmentDAO`, `JdbcTreatmentDAO`, `TreatmentService` retrieve active treatments, prices and durations |
| Dashboard | `DashboardStats`, `DashboardDAO`, `DashboardService`, `DashboardServlet` provide today's and upcoming counts using Sri Lankan day boundaries |
| Appointment data | `Appointment`, `AppointmentRequest`, `AppointmentDAO`, `JdbcAppointmentDAO`, `AppointmentService`, `BookingConflictException`, `AddAppointmentServlet` implement registration and transaction/error handling |
| JDBC utility | `ConnectionProvider` is an injectable functional interface; `DBConnection` implements it and explicitly loads the PostgreSQL driver |
| Views | `login.jsp`, `dashboard.jsp`, `add-appointment.jsp` live under WEB-INF; shared sidebar/topbar now use context-aware routes and escaped data |
| Browser assets | Existing CSS extended for the workspace; `appointment.js` toggles new-patient fields and displays saved patient details without replacing server validation |
| Deployment | `WEB-INF/web.xml` declares 20-minute sessions, HttpOnly cookies and cookie-only session tracking |

The complete new/modified/deleted inventory is in [phase-2-changes.txt](phase-2-changes.txt), and the complete source tree is in [directory-tree.txt](directory-tree.txt). JavaBean accessors on selected immutable records support JSP EL; they are not business logic.

## Authentication flow

1. Public GET `/login` receives a random CSRF token and the form.
2. The filter rejects POSTs with a missing/mismatched token.
3. LoginServlet passes the supplied username/password to AuthService.
4. JdbcUserDAO uses a PreparedStatement to retrieve the account.
5. PasswordHasher verifies a BCrypt hash; inactive accounts cannot authenticate. Unknown usernames and wrong passwords receive the same message. A dummy hash provides comparable hashing work for unknown usernames.
6. Successful login invalidates the old session, creates a new one and stores only `User(id, username, fullName, role)`, plus a new CSRF token. It redirects to the dashboard.
7. Logout is a CSRF-protected POST that invalidates the session and redirects to login. GET logout returns 405.

Password input is bounded to 72 UTF-8 bytes rather than silently truncated. Stored hash failures do not authenticate. Passwords/hashes are not stored in the session or rendered into views. Error logs contain SQL state, not credentials or raw query parameters. Cookie settings and no-store responses reduce session exposure; CSP and escaped output provide an additional browser-side defence.

BCrypt cost 12 and its input bound follow the relevant [OWASP password-storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html). BCrypt is implemented by Bouncy Castle's OpenBSDBCrypt, not a custom cryptographic algorithm; see the [official Bouncy Castle documentation](https://www.bouncycastle.org/documentation/documentation-java/).

## Appointment flow and rules

1. GET `/appointments/new` loads patients and active dentists/treatments through services and DAOs.
2. The user either selects an existing patient or supplies new patient details. The displayed appointment number is read-only and assigned on save.
3. The servlet passes raw form data and the authenticated user's ID to AppointmentService. Service code parses IDs/date/time and validates input.
4. The service opens one READ_COMMITTED transaction and locks the dentist row. It verifies that the dentist and treatment remain active, then derives the end time from the treatment duration.
5. The DAO detects any overlap with booked/completed visits; the service rejects the conflict before inserting a patient. The past-time rule is rechecked after lock acquisition.
6. An existing patient is looked up and validated, or a new patient is inserted on the same connection. The appointment stores foreign keys, not copied patient details.
7. PostgreSQL generates the SDC reference and the DAO returns it with INSERT RETURNING. The service commits both inserts or rolls back on failure.
8. Success redirects to the dashboard with an escaped confirmation message; validation errors redisplay the form with entered values and HTTP 400. Database failures return a generic HTTP 503 message.

| Rule | Enforcement |
| --- | --- |
| Patient required | New details or a valid existing patient ID |
| Name | 2 or more letters, maximum 120 characters; Unicode letters/marks, spaces, apostrophes, hyphens and periods |
| Address | Trimmed length 3–300; required for new and selected patients |
| Contact | Sri Lankan `0` plus 9 digits or `+94` plus 9 digits; spaces/hyphens removed; stored in +94 form for new patients |
| Dentist/treatment | Positive numeric ID that resolves to an active row |
| Date/time | Required, real calendar date, whole-minute precision, year 2000–9999 |
| Past appointment | Rejected using an injected Clock and Asia/Colombo interpretation |
| Double booking | Same dentist/start rejected; partial duration overlap also rejected; adjacent slots are allowed |
| Atomicity | Patient and appointment share connection, commit and rollback |
| Reference | Separate PostgreSQL sequence, UNIQUE column, SDC numeric format; sequence gaps are valid |

Registration uses the authenticated user ID, never a staff ID supplied by the form. Existing patient fields are not overwritten by new-patient fields in the request. Invalid numeric dropdown submissions redisplay safely using string-valued IDs, avoiding JSP numeric-coercion failures.

## Database changes

- Fresh schema adds `patients.address` with NOT NULL and length CHECK; tightens the phone CHECK.
- Existing databases use `002_patient_address_and_booking_guard.sql`. Legacy rows remain intact. NOT VALID checks enforce valid new/updated rows while allowing explicit backfill of historical data; README documents validation and SET NOT NULL afterward.
- `uq_appointments_dentist_start` is a partial unique index for BOOKED/COMPLETED visits. Cancelled/no-show visits do not reserve a slot in this phase.
- Seed provisions `sunrise.admin` with a generated BCrypt hash and activates only a previously unprovisioned account. Repeated seeding preserves existing hashes and reference data.
- Existing keys, normalized relationships, money types and database reference sequence are retained. There are no application triggers, stored functions or destructive table replacements.

The dentist lock serialises overlap checks among requests through this registration service. The database independently guards identical start times; arbitrary SQL writers with different start times could bypass the service's broader overlap rule. Future writers must use the same lock/check protocol or a later database interval constraint.

## Design patterns evidenced

- **MVC:** JSP views, servlet request controllers and immutable domain records.
- **DAO:** interfaces and JDBC implementations isolate persistence and PreparedStatements.
- **Service Layer:** input/business rules, authentication decisions, dashboard day boundaries and appointment transaction orchestration.
- Constructor injection supports tests with mocked DAOs and a fixed Clock. `Services` is a small composition root, not a claimed framework or GoF singleton. No artificial front controller is used.

## Verification observed

Final `mvn -B clean test` and `mvn -B clean package`: BUILD SUCCESS, 55 tests, 0 failures, 0 errors, 0 skipped. WAR exists at `target/sunrise-dental-clinic.war` (11,977,937 bytes when verified).

| JUnit class | Executions |
| --- | ---: |
| DatabaseConfigTest (retained Phase 1) | 9 |
| AuthServiceTest | 6 |
| PatientServiceTest | 12 |
| DashboardServiceTest | 1 |
| AppointmentServiceTest | 19 |
| AuthenticationFilterTest | 6 |
| SessionServletTest | 2 |
| Total | 55 |

The genuine double-booking red checkpoint is commit `38df028`, with one assertion failure because no ValidationException was thrown. The conflict lookup/decision was then implemented, and the same test passed. Raw red/green outputs are retained under docs/evidence; the red commit is intentionally not a deployable milestone.

The WAR was deployed on isolated GlassFish 7.1.0 (HTTP 19080) using an isolated PostgreSQL 15 cluster (127.0.0.1:55432) with a generated local password. The user's existing domain/database were not changed. Observed checks:

- 19 HTTP smoke assertions: public health/landing, protected routes/JSP, CSRF, generic login failure, usable seed login, session rotation, dashboard, database dropdowns, malformed input, creation/reference confirmation, conflict, patient reuse and logout.
- 9 database assertions: rerunnable seed counts, HTTP fixture row counts, actual rollback after both inserts, concurrent winner/loser, no orphan patient, SDC format, partial overlap rejection, adjacent slot/reference uniqueness and independent unique-index enforcement (some assertions cover more than one property).
- Migration applied to a separate copy of the Phase 1 schema containing a legacy patient. The row survived, new missing addresses were rejected, and verified backfill/constraint validation succeeded.
- Browser inspection of login and dashboard at default desktop width, appointment form at 390×844, existing-patient selection disabling new-patient inputs, and logout. No screenshot file is claimed; capture placeholders remain below.

A runtime login check initially exposed JDBC driver discovery under GlassFish. Explicitly loading `org.postgresql.Driver` fixed it; all 19 HTTP checks then passed. The CLI connection check also succeeded. Expression Language API is provided by GlassFish rather than bundled; PostgreSQL, BCrypt and JSTL are packaged. No real credentials, JUnit or Mockito are in the WAR.

Both isolated test servers were stopped after verification; their ignored fixture files remain in .runtime/. The delivered WAR is a build artifact, not an ongoing hosted deployment.

## Git history and delivery status

| Commit | Actual work |
| --- | --- |
| eb35a13 | Secure authentication and session handling |
| 591da47 | Dashboard and clinic reference data services |
| 38df028 | Genuine failing dentist-conflict test against registration scaffold |
| 39159cb | Transactional registration workflow, passing tests and runtime evidence |

A final documentation commit records this report, updated tree and test plan. No Phase 1 commits were squashed or rewritten. This checkout has no remote, despite the request mentioning an existing repository; its URL was requested. No push has occurred and no remote synchronisation is claimed.

## Remaining manual/deployment items and limitations

- Apply migration/backfill to the user's actual Phase 1 database and deploy the WAR to their normal domain; isolated verification does not modify or certify that environment.
- Configure HTTPS, secure cookies and proxy settings for deployment; those were not tested over HTTPS. Wait through a full 20-minute idle expiry as a manual check.
- Capture the assessment screenshots below using suitable fictional data. Broader accessibility, browser compatibility and load testing remain future verification.
- No login rate limiter, account administration, password reset, role-specific authorisation or production credential provisioning is implemented. The demo account is only for local testing.
- Patient dropdown loads the current patient list; pagination and record editing are outside scope. Legacy incomplete patients require administrator backfill.
- No clinic opening-hours/holiday policy was supplied; this phase rejects past and conflicting slots but does not invent those scheduling rules.
- Search/details, billing, reports, help, advanced REST, GitHub Actions, final UML and the final report remain unimplemented.

## Runtime screenshot placeholders

- [ ] Login page and generic incorrect-credentials message.
- [ ] Dashboard showing staff identity and real counts.
- [ ] New-patient form and database dentist/treatment choices.
- [ ] Existing-patient selection and saved address/contact.
- [ ] Successful SDC appointment reference confirmation.
- [ ] Conflict/past-date/contact validation messages.
- [ ] Anonymous protected-route redirect and logout.
- [ ] Mobile form at 390px and PostgreSQL normalized rows.
