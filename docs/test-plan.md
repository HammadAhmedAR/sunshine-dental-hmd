# Phase 1 test plan and evidence

Date: 2026-09-05. Environment: Windows 11, Microsoft OpenJDK 17.0.18, Maven 3.9.12.

## Automated configuration checks

`DatabaseConfigTest` has nine executions in one class:

| Rule | Executions | Expected |
| --- | ---: | --- |
| Valid URL and username; preserve password whitespace | 1 | Values retained, username trimmed |
| Missing URL, username or password | 3 | IllegalArgumentException |
| Blank URL, username or password | 3 | IllegalArgumentException |
| Non-PostgreSQL URL | 1 | Rejection without including supplied URL |
| Unchanged example password | 1 | Rejection |

The test file was created before DatabaseConfig. `mvn -B test` then failed at test compilation with six missing-symbol diagnostics because DatabaseConfig did not exist. This is the observed red step (compilation failure, not failed assertions). After implementation, `mvn -B clean test` passed all nine executions: zero failures, errors or skips. This short test-first cycle is documented here; no historical commits have been fabricated.

Surefire regenerates machine-readable XML and a text report under `target/surefire-reports/`. Maven package also runs the tests. Tests need no database or application server.

## Manual checks still required

| Check | Expected | Status |
| --- | --- | --- |
| Run schema against an empty PostgreSQL database | Six tables and associated constraints/indexes | Not run |
| Run seed twice | One disabled user, two dentists, five treatments; no duplicates | Not run |
| Try invalid foreign keys, negative amounts, second bill for appointment | Database rejects inserts | Not run |
| Run TestConnection with external credentials | Exit 0 and confirmation | Not run |
| Deploy WAR on GlassFish 7 | Deployment succeeds | Not run |
| Request landing page | JSP renders with shared sidebar/header | Not run |
| Request health endpoint | HTTP 200, application/json, expected body | Not run |
| Inspect desktop and mobile widths, keyboard navigation | Readable layout, visible focus, no broken navigation | Not run |

Later milestones should add service tests with fake DAOs, transactional database integration checks, authentication tests, collision/overlap tests and billing boundary cases. No service or DAO functionality exists yet, so their test folders are placeholders.

## Phase 2: genuine test-first checkpoint

The appointment registration scaffold was built with transaction handling and input validation before its conflict rule. A test stubbed the DAO to report an occupied dentist slot and expected ValidationException.

Command: `mvn -B "-Dtest=AppointmentServiceTest#rejectsDoubleBooking" test`.
Observed on 2026-09-05: 1 test, 1 assertion failure, 0 errors. The failure was: "Expected com.sunrise.clinic.service.ValidationException to be thrown, but nothing was thrown." Full unedited command output is retained in `docs/evidence/phase-2-tdd-red.txt`. This checkpoint intentionally fails that test; it is not a deployable milestone.

## Phase 2 final automated and runtime results

The conflict decision was implemented after the red checkpoint. The identical targeted test passed: 1 execution, 0 failures/errors/skips. See `docs/evidence/phase-2-tdd-green.txt`. No test was weakened to obtain the green result.

Final clean builds both passed: `mvn -B clean test` and `mvn -B clean package`. There are 55 JUnit executions in seven classes: DatabaseConfigTest 9, AuthServiceTest 6, PatientServiceTest 12, DashboardServiceTest 1, AppointmentServiceTest 19, AuthenticationFilterTest 6 and SessionServletTest 2. This adds 46 executions to Phase 1. No getter/setter tests were added.

Appointment coverage includes valid creation, past dates, missing/invalid patient details, missing dentist/treatment/date/time, invalid calendar dates, inactive references, existing-patient reuse, unknown patient, double booking, failure of either insert, rollback, database uniqueness-race translation, staff identity and minute precision. Dashboard testing fixes Clock at a UTC instant on the next Sri Lankan day. Security tests cover credential outcomes, UTF-8 password bounds, malformed hashes, access filtering, CSRF, session replacement and logout.

The 55 tests run without GlassFish or PostgreSQL using constructor-injected mocks and a fixed Clock. Raw final Maven outputs are `phase-2-clean-test.txt` and `phase-2-clean-package.txt` under docs/evidence.

Separate observed runtime verification used isolated PostgreSQL 15 and GlassFish 7.1.0, with no changes to the user's existing domain/database:

| Evidence | Result |
| --- | --- |
| phase-2-http-smoke.txt | 19 passing HTTP assertions |
| phase-2-database-check.txt | 9 passing database/concurrency/rollback assertions |
| phase-2-migration-check.txt | Legacy preservation, new address constraint and backfill validation passed |
| Browser inspection | Login, dashboard, existing-patient behaviour, mobile form at 390x844 and logout inspected |

These counts are separate from JUnit, and the runtime fixture is reproducible using scripts/README.md. A runtime JDBC driver-loading issue was found and fixed before the final checks. Screenshots were viewed during browser inspection, but no image files have been added; the Phase 2 evidence document retains explicit capture placeholders. Phase 1's earlier 'not run' table above is historical; it is not the current Phase 2 status.

Remaining checks are the actual user's deployment/migration, HTTPS/secure-cookie configuration, full idle timeout, broader browser/accessibility/load verification and assessment screenshot capture. There is no final assignment report or UML in this milestone.

## Revised Phase 2 request: additional test-first checkpoint

Existing double-booking RED/GREEN evidence remains intact in commit 38df028 and its subsequent implementation; it was not recreated. Before adding blank-password validation, the new targeted command `mvn -B "-Dtest=AuthServiceTest#blankPasswordRejectedBeforeDao" test` ran four cases with two genuine failures: whitespace-only passwords reached UserDAO. Null and empty inputs already passed. Output is preserved in `evidence/phase-2-blank-password-red.txt`. This checkpoint adds a previously missing validation rule rather than removing an implemented rule to manufacture a failure.

## Revised Phase 2: final results

The blank-password fix passed all four targeted cases, with the seven new reference tests also passing in the same run (11 total). Final `mvn -B clean test` and `mvn -B clean package` each passed 71 executions, zero failures/errors/skips. Current logs are `phase-2-revision-clean-test.txt` and `phase-2-revision-clean-package.txt`. The latest additions are eight blank-credential cases, seven reference-generator checks and one DAO/time-zone binding check (16 new executions).

Migration 003 was applied to the isolated PostgreSQL fixture. A real registration saved APT-2026-00007 and existing SDC references remained intact; the outputs are in `phase-2-reference-migration.txt` and `phase-2-reference-check.txt`. Earlier 55-test and HTTP/browser logs remain historical, not re-labelled as results of this revision. The current completion record is [phase-2-report.md](phase-2-report.md).
