# Phase 1 completion report

Phase 1 implementation only. No Phase 2 workflow, final UML, final assessment report, CI workflow or remote deployment has been added.

## Files and responsibilities

| File or group | Purpose |
| --- | --- |
| `pom.xml` | Java 17 WAR, pinned dependencies/plugins, final artifact name, excludes local credentials |
| `.gitignore` | Excludes target, IDE metadata, actual db.properties, logs and temporary files |
| `README.md` | Scope, architecture, build, database setup, CLI check and deployment instructions |
| `config/DatabaseConfig.java` | Loads external UTF-8 properties; validates required settings without echoing credentials |
| `util/DBConnection.java` | Opens fresh JDBC connections with a connection timeout; callers own closing |
| `util/TestConnection.java` | Optional CLI database smoke check with nonzero failure exit |
| `rest/RestApplication.java` | Registers `/api` application path |
| `rest/HealthResource.java` | GET `/health` JSON liveness response |
| Eight `package-info.java` files | Define package responsibilities without inventing unused classes |
| `db.properties.example` | Safe example; never used as an automatic credential fallback |
| `database/schema.sql` | Transactional, non-destructive foundation DDL |
| `database/seed.sql` | Rerunnable fictional local seed data; disabled administrator |
| `index.jsp` | Clinic landing page and honest staff entry placeholder |
| `fragments/sidebar.jspf`, `topbar.jspf` | Shared navigation and header fragments for the current root page |
| `app.css` | Responsive shell, cards, buttons, controls, tables, messages, badges and keyboard focus |
| `auth.css` | Landing/entry layout and responsive styling |
| `.gitkeep` files | Track deliberately empty future view, JS and service/DAO test directories |
| `DatabaseConfigTest.java` | Nine meaningful configuration validation checks in one JUnit class |
| `docs/test-plan.md` | Test-first observations, automated results and pending runtime checks |
| `docs/directory-tree.txt` | Complete project source tree; generated build/Git internals excluded |

Shared fragment paths are currently relative to the root landing page. When servlet views are introduced, use context-aware URLs rather than copying those root-relative assumptions into nested routes.

## Dependencies

| Maven dependency | Version | Scope |
| --- | --- | --- |
| jakarta.servlet:jakarta.servlet-api | 6.0.0 | provided |
| jakarta.ws.rs:jakarta.ws.rs-api | 3.1.0 | provided |
| org.postgresql:postgresql | 42.7.8 | compile (packaged) |
| org.junit.jupiter:junit-jupiter | 5.11.4 | test |

Plugins: Compiler 3.13.0, Surefire 3.5.2, WAR 3.5.1. JSP is supplied by GlassFish; no standalone JSP implementation is packaged. JDBC brings its transitive Checker Qual annotations library. No prohibited frameworks are used.

Build plugin setup was checked against the [official Maven WAR documentation](https://maven.apache.org/plugins/maven-war-plugin/usage.html); JDBC background is available in the [official pgJDBC documentation](https://jdbc.postgresql.org/).

## Database relationships and constraints

| Parent | Relationship | Child |
| --- | --- | --- |
| patients | 1 to 0..many | appointments.patient_id |
| dentists | 1 to 0..many | appointments.dentist_id |
| treatments | 1 to 0..many | appointments.treatment_id |
| users | 1 to 0..many | appointments.created_by |
| appointments | 1 to 0..1 | bills.appointment_id (UNIQUE) |
| users | 1 to 0..many | bills.issued_by |

All primary keys are generated BIGINT identities. Each appointment has exactly one patient, dentist and treatment in this foundation. A separate sequence generates distinct `SDC-1`, `SDC-2`, etc. references; gaps are valid and references must not be used as counts. Future DAO inserts should omit appointment_number and use INSERT RETURNING. No MAX(id)+1 generation, trigger or stored function is used.

Money uses NUMERIC(12,2), with nonnegative values and bounded discounts; NaN is explicitly rejected. Bills preserve the issued subtotal independently of later treatment price changes and derive total as subtotal minus discount. LKR is enforced. Times use TIMESTAMPTZ; future UI/service code should display Asia/Colombo time. Appointment time ordering and allowed status values are constrained. Foreign keys do not cascade-delete clinical or billing records. Patient phone numbers are deliberately not unique because families may share one.

Overlapping appointment prevention, service-level validation, richer email/phone checks, transaction handling and authentication are later milestone work. The schema does not claim to solve scheduling concurrency yet.

## Verification and limits

`mvn -B clean test`: BUILD SUCCESS; 9 tests, 0 failures, 0 errors, 0 skipped.

`mvn -B clean package`: BUILD SUCCESS; the same nine tests passed. Confirmed `target/sunrise-dental-clinic.war` exists. Archive inspection confirmed the PostgreSQL driver and Checker Qual dependency are packaged, while Servlet/REST API jars, JUnit and actual db.properties are absent. The safe example properties file is included. `git diff --check` passed.

The initial Maven build needed access to the standard user Maven cache; after the permitted retry, dependencies resolved. The intermediate test-first compilation failure was intentional and was fixed by implementing DatabaseConfig.

GlassFish 7 files are present locally, but deployment, JSP compilation/rendering, responsive browser inspection, HTTP routing, PostgreSQL schema execution, seed execution and a live JDBC connection have not been verified. No database credentials were supplied and psql was not found on PATH. Unit-test success does not establish these runtime results.

The first commit is `7c2ec09 chore: initialize Sunrise Dental Clinic Jakarta EE project`. The second milestone commit is `feat: add database and REST application foundation`; use `git log --oneline` for its generated hash. Both represent real work in this session.

No GitHub remote exists. Create an empty `sunrise-dental-clinic` repository on GitHub; its URL is needed before configuring origin and pushing. Nothing has been pushed.
