# Isolated Phase 2 runtime checks

These are explicit manual integration checks, separate from the 55 JUnit tests. They create fictional rows. Never run them against real clinic data.

The observed fixture used PostgreSQL 15 on `127.0.0.1:55432`, database `sunrise_dental_clinic`, and GlassFish 7.1.0 HTTP port `19080`, admin port `19048`, context `/sunrise-dental-clinic`. Runtime files and generated credentials are stored in ignored `.runtime/`. The normal PostgreSQL server and GlassFish domain are unaffected.

To reproduce from a fresh, separate fixture:

1. Create an empty isolated PostgreSQL database and apply current schema.sql, then seed.sql twice.
2. Set SUNRISE_DB_CONFIG to its external configuration file before starting an isolated GlassFish domain; deploy the WAR at the context above on port 19080.
3. Run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/phase2-http-smoke.ps1`. This process-only execution-policy option does not change the machine policy. The script expects a fresh fixture and creates one patient with two visits.
4. Set SUNRISE_DB_CONFIG in your terminal, then run:

```powershell
java -cp "target/classes;target/sunrise-dental-clinic/WEB-INF/lib/*" scripts/Phase2DatabaseCheck.java
```

The Java check requires the preceding HTTP fixture. It tests rollback after both inserts, concurrent service calls, overlapping/adjacent slots and database uniqueness. It leaves fictional rows for inspection. Run the pair once per fresh fixture; they are not idempotent and do not delete existing data.

Saved observed output is under `docs/evidence/`. Windows PowerShell 5 renders some native stderr warnings as NativeCommandError records; Maven BUILD SUCCESS/exit status and the explicit PASS/FAIL assertions determine the result. The deliberate TDD red output must not be confused with final build results.

The migration check used a separate database built from Phase 1 commit 343704c's schema, inserted one fictional legacy patient, applied migration 002, verified that no address was invented, rejected a new missing address, then backfilled and validated the checks. This was an observed manual database check rather than a JUnit test.
