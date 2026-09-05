package com.sunrise.clinic.util;

import com.sunrise.clinic.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/** Opens a fresh connection; callers must close it with try-with-resources. */
public final class DBConnection implements ConnectionProvider {
    private final DatabaseConfig config;

    public DBConnection(DatabaseConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    public Connection getConnection() throws SQLException {
        // GlassFish may initialise DriverManager before it sees this WAR's driver.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("PostgreSQL JDBC driver is unavailable.", "08001", exception);
        }
        Properties properties = new Properties();
        properties.setProperty("user", config.getUsername());
        properties.setProperty("password", config.getPassword());
        properties.setProperty("connectTimeout", "5");
        return DriverManager.getConnection(config.getUrl(), properties);
    }
}

