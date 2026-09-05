# Sunrise Dental Clinic

Original CIS6003 Advanced Programming project. Phase 2 implements staff authentication, a dashboard, database-backed clinic reference data and transactional appointment registration. Phase 1 history remains intact. No code was copied from the reference project.

## Build

Requires Java 17 and Maven 3.9+:

```powershell
mvn clean test
mvn clean package
```

Output: `target/sunrise-dental-clinic.war`. Both commands passed with 71 tests, no failures, errors or skips. GlassFish 7.1.0 and PostgreSQL 15 were also exercised in an isolated local fixture. See [current Phase 2 report](docs/phase-2-report.md).

## Database setup

For a **new empty database**, create `sunrise_dental_clinic`, then run:

```powershell
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/schema.sql
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/seed.sql
```

For an **existing Phase 1 database**, run the migration once instead of schema.sql, then seed:

```powershell
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/migrations/002_patient_address_and_booking_guard.sql
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/migrations/003_appointment_reference_format.sql
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/seed.sql
```

If migration 002 was already applied during Phase 2, run only migration 003 before deploying this revised WAR. Fresh schema.sql installations need neither migration. No existing tables are dropped. Migration preserves legacy patient rows without inventing addresses. An administrator must backfill verified addresses and correct invalid old phone numbers before those records can be selected for a new appointment. After backfilling:

```sql
ALTER TABLE patients VALIDATE CONSTRAINT patients_address_required;
ALTER TABLE patients VALIDATE CONSTRAINT patients_sri_lankan_phone;
ALTER TABLE patients ALTER COLUMN address SET NOT NULL;
```

The new checks apply immediately to inserted/updated rows, even before validation of historical rows. If existing active appointments duplicate a dentist/start slot, the migration fails atomically; reconcile those records before retrying. Do not rerun schema.sql or the migration on an already-updated database. Seed is rerunnable and does not reset provisioned accounts.

## Local testing credentials

Development fixture only:

- Username: `sunrise.admin`
- Password: `SunriseLocal!2026`

The database contains only a BCrypt cost-12 hash. Seed activates the unprovisioned Phase 1 account if its hash is NULL and it is inactive; existing non-NULL hashes are preserved. Do not deploy this known demonstration account to a public clinic environment. Account management, password reset and rate limiting are not implemented in Phase 2.

## External database configuration

```powershell
Copy-Item src/main/resources/db.properties.example db.properties
# Edit db.properties with your local PostgreSQL credentials.
$env:SUNRISE_DB_CONFIG = (Resolve-Path db.properties).Path
```

Set this environment variable in the process that starts GlassFish. Alternatively pass the JVM system property `sunrise.db.config`, which takes precedence. Use forward slashes in Windows paths inside Java properties. Credentials are not bundled: actual db.properties is Git-ignored and WAR-excluded; .runtime/ is also ignored. The example password `change_me` is rejected.

After packaging, the optional CLI connectivity check is:

```powershell
java -cp "target/classes;target/sunrise-dental-clinic/WEB-INF/lib/*" com.sunrise.clinic.util.TestConnection
```

Connections use JDBC PreparedStatement in DAOs. DBConnection explicitly loads the WAR's PostgreSQL driver because GlassFish may initialise DriverManager before the web application is loaded.

## GlassFish deployment

With your configured GlassFish 7 domain running on Java 17:

```powershell
C:/Servers/glassfish7/glassfish/bin/asadmin.bat deploy --contextroot sunrise-dental-clinic target/sunrise-dental-clinic.war
# To update an existing deployment, add --force=true.
```

Default port URLs:

| URL | Access |
| --- | --- |
| `/sunrise-dental-clinic/` | Public landing page |
| `/sunrise-dental-clinic/login` | Public staff login; POST requires a CSRF token |
| `/sunrise-dental-clinic/dashboard` | Signed-in staff |
| `/sunrise-dental-clinic/appointments/new` | Signed-in staff; GET form / POST registration |
| `/sunrise-dental-clinic/logout` | Signed-in staff; CSRF-protected POST only |
| `/sunrise-dental-clinic/api/health` | Public JSON liveness, independent of database readiness |

Session timeout is 20 minutes. Cookies use HttpOnly and SameSite=Lax, with COOKIE-only tracking. For HTTPS deployment set `SUNRISE_SECURE_COOKIES=true` before starting the server. HTTPS/proxy configuration remains deployment work; local verification used HTTP.

## Architecture and scope

JSP -> Servlet -> Service -> DAO -> JDBC -> PostgreSQL. MVC, DAO and Service Layer patterns now have concrete implementations. DAO interfaces and constructor injection support unit tests with Mockito; no DI framework or artificial front controller is used. Services own validation and transaction boundaries; DAOs contain SQL. Domain records are immutable. JSP uses JSTL to escape displayed data.

Patient selection reuses an existing foreign key; new patient creation and appointment insertion share one JDBC transaction. Dentist row locks serialise registration, the DAO checks time overlap, and a partial unique index independently prevents equal dentist/start slots. Treatment duration comes from PostgreSQL. All appointment dates/times are interpreted in Asia/Colombo. New references use `APT-YYYY-NNNNN`; existing SDC references remain valid. The saved value is returned by INSERT RETURNING.

Not implemented: appointment search/details, billing, reports, help content, dentist administration, advanced REST, GitHub Actions, final UML or final assignment report. Unimplemented navigation entries remain inactive.

## Evidence and Git

- [Phase 2 report](docs/phase-2-report.md)
- [Test plan and genuine red/green evidence](docs/test-plan.md)
- [Complete current source tree](docs/directory-tree.txt)
- [New/modified/deleted files relative to Phase 1](docs/phase-2-changes.txt)
- [Manual isolated-runtime verification instructions](scripts/README.md)

The connected origin is https://github.com/HammadAhmedAR/sunshine-dental-hmd.git. Use git status -sb and git log to check current synchronisation. Phase 1's report is preserved as a historical record of that implementation session; it does not describe the current Phase 2 scope.

# sunshine-dental-hmd
JEE Application 
