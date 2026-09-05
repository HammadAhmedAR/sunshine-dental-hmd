# Login failure diagnosis

## Confirmed root cause

Failing step: **2 — DatabaseConfig.load/fromProperties configuration validation**, before JDBC.

The running GlassFish domain1 deployment contained an example database password in
`applications/sunrise-dental-clinic/WEB-INF/classes/db.properties`.
The workspace resource and current build contained a non-placeholder local configuration.
Running the diagnostic against the deployed classes reproduced:

```text
java.lang.IllegalArgumentException: Replace the example db.password before connecting.
```

A real HTTP login request reproduced status 503 and the reported unavailable message.
This was a stale deployed configuration, not a BCrypt, schema, session or driver defect.

## Verified flow and error propagation

AuthenticationFilter checks POST CSRF -> LoginServlet.doPost reads username/password ->
Services.auth -> AuthService.authenticate -> JdbcUserDAO.findByUsername ->
Services connection provider -> DatabaseConfig.load -> DBConnection.getConnection ->
prepared users query -> PasswordHasher.verify -> safe User -> fresh HttpSession ->
dashboard redirect. AuthenticationFilter reads the same `loggedUser` attribute.

DatabaseConfig already used the application classloader. It successfully located the deployed
file, then fromProperties rejected its example password. Services converted that exception
to SQLException with SQLState 08001, originally discarding the cause. LoginServlet's
SQLException catch logged only that state, returned HTTP 503, and displayed the safe generic
message. No database connection or BCrypt verification occurred on this failing path.

Services is lazily stored in ServletContext; database configuration is loaded per connection,
not during static initialization. No initialization failure or attribute mismatch was found.

## Changes made in this diagnosis

- DatabaseConfig.java: package-visible ClassLoader overload permits isolated resource tests;
  public load still uses DatabaseConfig's own classloader.
- Services.java: retain the original configuration exception as SQLException's cause.
- LoginServlet.java: log the cause class and a safe recognized placeholder-password reason;
  arbitrary JDBC messages, passwords and hashes are not logged. UI error stays generic.
- DatabaseConfigTest.java: replace the test that expected the developer's actual resource to
  contain an example password with independent in-memory configuration tests. Cover valid
  classpath data, missing resource and deployed example-password rejection.
- Cleanly undeployed/redeployed only sunrise-dental-clinic using the newly built WAR.

The pre-existing pom.xml, resource-example and classpath-loading changes were retained.
Actual local database credentials were neither changed nor printed. No account, hash,
schema, appointment, billing, reporting, REST or UI changes were needed.

## Actual connectivity and account checks

Configuration: classpath db.properties; URL
`jdbc:postgresql://localhost:5432/sunrise_dental_clinic`; database username `postgres`;
password present (value withheld).

JDBC connection succeeded. SELECT current_database(), current_user returned
sunrise_dental_clinic / postgres. SELECT 1 succeeded; public.users exists.
JdbcUserDAO's prepared `WHERE username = ?` query and all selected column names worked.

Both sunrise.admin and staff were found and active, with ADMIN and STAFF roles respectively.
The documented development password for sunrise.admin matched its stored hash using
the application's Bouncy Castle OpenBSDBCrypt implementation (bcprov-jdk18on 1.83).
AuthService accepted that account and rejected an incorrect password.
The staff password was not tested or changed.

The WAR contains WEB-INF/classes/db.properties, postgresql-42.7.8.jar and
bcprov-jdk18on-1.83.jar. Packaged configuration equals the source resource.
After redeployment, deployed db.properties, DatabaseConfig.class and LoginServlet.class
match the new build. No dependency/classloader change was necessary.

## Automated and HTTP verification

- mvn -B clean test: BUILD SUCCESS; 109 tests, 109 passed, 0 failures/errors/skips.
- mvn -B clean package: BUILD SUCCESS; 109 tests, 0 failures/errors/skips.
- WAR: target/sunrise-dental-clinic.war.
- Running domain: domain1; admin port 4848; HTTP port 8080.
- Application name/context root: sunrise-dental-clinic.
- Latest WAR cleanly redeployed successfully.

Actual HTTP checks: landing 200, health JSON UP, login page with CSRF token,
incorrect-password generic credentials error, valid-login dashboard redirect,
session identity rotation, dashboard 200, protected appointment page 200,
POST logout redirect, and subsequent protected-page redirect all passed.
These are HTTP checks, not browser appearance or print-preview claims.

Private diagnostic scripts/results remain ignored under .runtime:
LoginDiagnostic.java, login-http-check.ps1, login-http-result.txt,
login-clean-test.txt and login-clean-package.txt.
GlassFish log: C:/Servers/glassfish7/glassfish/domains/domain1/logs/server.log.

## Remaining action

Open http://localhost:8080/sunrise-dental-clinic/login and sign in using the documented
development account. No additional configuration fix is required for the tested deployment.
A future local configuration change requires rebuilding and redeploying, because this
configuration is packaged in the WAR. Keep this credential-bearing WAR private.

No commit or push was performed. Existing untracked archives were left untouched.
The earlier final-closure/documentation task remains paused.
