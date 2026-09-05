# Sunrise Dental Clinic

Original CIS6003 Advanced Programming assessment project. Phase 1 establishes a Java 17 / Jakarta EE 10 compatible Maven WAR. No reference implementation files were used.

## Build

Requires JDK 17 and Maven 3.9+. Run `mvn clean test` and `mvn clean package`.
The artifact is `target/sunrise-dental-clinic.war`, intended for GlassFish 7.

## Scope

The landing page and shared JSP/CSS layout are foundations only. Navigation labels are inactive placeholders; the staff entry link explains that authentication comes later. No patient data, login workflow, appointments, billing, reports or help content are implemented.

## Architecture

Planned flow: JSP → Servlet → Service → DAO → JDBC → PostgreSQL; REST resources will share the same services. Package documentation reserves these responsibilities without adding artificial implementations. MVC, DAO and Service Layer are planned patterns, not complete Phase 1 implementations. No front controller or singleton pattern is claimed.

Future services should accept DAO dependencies through constructors. Unit tests can supply in-memory fakes without a database, JSP engine or GlassFish. Database integration tests belong in a separate later milestone.

## Database setup (manual, not performed by the build)

Create an empty PostgreSQL database, then run from this project directory:

```powershell
createdb -U postgres sunrise_dental_clinic
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/schema.sql
psql -U postgres -d sunrise_dental_clinic -v ON_ERROR_STOP=1 -f database/seed.sql
Copy-Item src/main/resources/db.properties.example db.properties
$env:SUNRISE_DB_CONFIG = (Resolve-Path db.properties).Path
```

Edit the local file with your database credentials before connecting; `change_me` is rejected. Do not commit it. For deployment use an external file readable only by the server account, preferably with a dedicated least-privilege PostgreSQL user. The JVM property `sunrise.db.config` takes precedence over `SUNRISE_DB_CONFIG`. Configuration loads lazily, so the landing page and liveness endpoint do not require a database. The actual file is excluded from the WAR even if placed in main resources.

After packaging, run the optional connection check in PowerShell:

```powershell
java -cp "target/classes;target/sunrise-dental-clinic/WEB-INF/lib/*" com.sunrise.clinic.util.TestConnection
```

The seed account `sunrise.admin` is inactive and has a NULL hash; it cannot be used to sign in. In a later milestone, provision a salted adaptive password hash (never plaintext) before activation. The database only enforces presence for active accounts; algorithm verification belongs to the future authentication service.

## GlassFish 7 deployment (manual)

Start a configured GlassFish 7 domain using JDK 17, then run:

```powershell
C:/Servers/glassfish7/glassfish/bin/asadmin.bat deploy --contextroot sunrise-dental-clinic target/sunrise-dental-clinic.war
```

For the default HTTP port, open `http://localhost:8080/sunrise-dental-clinic/`.
Request `GET http://localhost:8080/sunrise-dental-clinic/api/health`:

```json
{"status":"UP","application":"Sunrise Dental Clinic"}
```

This reports application liveness, not database readiness. Servlet and REST APIs are supplied by GlassFish, whose JSP engine compiles the views at runtime. Maven packaging alone does not verify JSP rendering or REST routing.

## Development evidence

See [Phase 1 report](docs/phase-1-report.md), [complete source tree](docs/directory-tree.txt) and [test plan](docs/test-plan.md). No GitHub remote is configured; create an empty GitHub repository named `sunrise-dental-clinic` before adding an origin and pushing. No push or GitHub Actions setup has been performed.
