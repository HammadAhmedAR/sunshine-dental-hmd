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
