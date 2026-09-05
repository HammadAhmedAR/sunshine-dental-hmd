package com.sunrise.clinic.config;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.service.*;
import com.sunrise.clinic.util.*;
import jakarta.servlet.ServletContext;
import java.sql.SQLException;

/** Small explicit composition root; no dependency-injection framework is needed. */
public final class Services {
    private final ConnectionProvider connections = () -> {
        try {
            return new DBConnection(DatabaseConfig.load()).getConnection();
        } catch (java.io.IOException | IllegalArgumentException | IllegalStateException exception) {
            throw new SQLException("Database configuration is unavailable.", "08001");
        }
    };
    private final AuthService auth = new AuthService(new JdbcUserDAO(connections), new PasswordHasher());
    public AuthService auth() { return auth; }
    public ConnectionProvider connections() { return connections; }
    public PatientService patients() { return new PatientService(connections, new JdbcPatientDAO()); }
    public DentistService dentists() { return new DentistService(connections, new JdbcDentistDAO()); }
    public TreatmentService treatments() { return new TreatmentService(connections, new JdbcTreatmentDAO()); }
    public DashboardService dashboard() {
        return new DashboardService(connections, new DashboardDAO(), java.time.Clock.systemUTC());
    }

    public static Services get(ServletContext context) {
        synchronized (context) {
            Services services = (Services) context.getAttribute(Services.class.getName());
            if (services == null) {
                services = new Services();
                context.setAttribute(Services.class.getName(), services);
            }
            return services;
        }
    }
}
