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
    public AppointmentService appointments() {
        return new AppointmentService(connections, new JdbcPatientDAO(), new JdbcDentistDAO(),
                new JdbcTreatmentDAO(), new JdbcAppointmentDAO(), patients(), java.time.Clock.systemUTC());
    }
    public AppointmentQueryService appointmentQueries() {
        return new AppointmentQueryService(connections, new JdbcAppointmentDAO());
    }
    public AppointmentManagementService appointmentManagement() {
        return new AppointmentManagementService(connections, new JdbcAppointmentDAO(), new JdbcDentistDAO(),
                new JdbcTreatmentDAO(), java.time.Clock.systemUTC());
    }
    public BillService bills() {
        return new BillService(connections, new JdbcAppointmentDAO(), new JdbcBillDAO(), java.time.Clock.systemUTC());
    }
    public ReportService reports() { return new ReportService(connections, new JdbcReportDAO()); }

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
