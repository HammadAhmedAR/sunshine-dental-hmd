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
