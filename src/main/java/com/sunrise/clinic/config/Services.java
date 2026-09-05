package com.sunrise.clinic.config;

import com.sunrise.clinic.dao.JdbcUserDAO;
import com.sunrise.clinic.service.AuthService;
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
