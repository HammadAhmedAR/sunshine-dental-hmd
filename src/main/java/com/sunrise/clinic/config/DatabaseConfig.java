package com.sunrise.clinic.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Immutable configuration. Credentials are read from an external UTF-8 file. */
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
        String file = System.getProperty("sunrise.db.config");
        if (file == null || file.isBlank()) {
            file = System.getenv("SUNRISE_DB_CONFIG");
        }
        if (file == null || file.isBlank()) {
            throw new IllegalStateException("Set SUNRISE_DB_CONFIG or -Dsunrise.db.config to an external db.properties file.");
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            properties.load(reader);
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
        if (password.trim().equals("change_me")) {
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
