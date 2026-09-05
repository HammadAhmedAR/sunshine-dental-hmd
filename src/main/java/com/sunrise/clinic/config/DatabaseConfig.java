package com.sunrise.clinic.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Immutable configuration. Credentials are read from db.properties on the classpath. */
public final class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;

    private DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig load() throws IOException {
        return load(DatabaseConfig.class.getClassLoader());
    }

    // Package-visible for isolated resource-loading tests; production uses this class's loader.
    static DatabaseConfig load(ClassLoader classLoader) throws IOException {
        InputStream stream = classLoader != null
                ? classLoader.getResourceAsStream("db.properties")
                : DatabaseConfig.class.getResourceAsStream("/db.properties");
        if (stream == null) {
            throw new IllegalStateException("Database configuration file not found: db.properties");
        }
        Properties properties = new Properties();
        try (InputStream in = stream) {
            properties.load(in);
        } catch (IOException exception) {
            throw new IOException("Failed to read database configuration: db.properties", exception);
        }
        return fromProperties(properties);
    }

    public static DatabaseConfig fromProperties(Properties properties) {
        String url = required(properties, "db.url").trim();
        String username = required(properties, "db.username").trim();
        String password = required(properties, "db.password");
        if (!url.startsWith("jdbc:postgresql://")) {
            throw new IllegalArgumentException("db.url must be a PostgreSQL JDBC URL.");
        }
        if (password.trim().equals("change_me") || password.trim().equals("REPLACE_WITH_LOCAL_POSTGRES_PASSWORD")) {
            throw new IllegalArgumentException("Replace the example db.password before connecting.");
        }
        return new DatabaseConfig(url, username, password);
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing or blank setting: " + key);
        }
        return value;
    }

    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
