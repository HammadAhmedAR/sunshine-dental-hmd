package com.sunrise.clinic.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {
    private Properties validProperties() {
        Properties properties = new Properties();
        properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/sunrise_dental_clinic");
        properties.setProperty("db.username", " sunrise ");
        properties.setProperty("db.password", " secret with spaces ");
        return properties;
    }

    @Test
    void preservesPasswordWhitespaceWhileTrimmingUsername() {
        DatabaseConfig config = DatabaseConfig.fromProperties(validProperties());
        assertEquals("sunrise", config.getUsername());
        assertEquals(" secret with spaces ", config.getPassword());
        assertEquals("jdbc:postgresql://localhost:5432/sunrise_dental_clinic", config.getUrl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"db.url", "db.username", "db.password"})
    void rejectsMissingSettings(String key) {
        Properties properties = validProperties();
        properties.remove(key);
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.fromProperties(properties));
    }

    @ParameterizedTest
    @ValueSource(strings = {"db.url", "db.username", "db.password"})
    void rejectsBlankSettings(String key) {
        Properties properties = validProperties();
        properties.setProperty(key, "  ");
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.fromProperties(properties));
    }

    @Test
    void rejectsNonPostgresUrlWithoutExposingItsValue() {
        Properties properties = validProperties();
        properties.setProperty("db.url", "jdbc:mysql://private-host/secret");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> DatabaseConfig.fromProperties(properties));
        assertFalse(error.getMessage().contains("private-host"));
    }

    @Test
    void rejectsExamplePassword() {
        Properties properties = validProperties();
        properties.setProperty("db.password", "change_me");
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.fromProperties(properties));
    }
}
