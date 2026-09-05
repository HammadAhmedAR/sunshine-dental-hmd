package com.sunrise.clinic.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
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

        properties.setProperty("db.password", "REPLACE_WITH_LOCAL_POSTGRES_PASSWORD");
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.fromProperties(properties));
    }

    @Test
    void loadFromClasspathReadsDbProperties() throws Exception {
        DatabaseConfig config = DatabaseConfig.load(loader("db.url=jdbc:postgresql://localhost:5432/test_clinic\n"
                + "db.username=test_user\ndb.password=test-only-value\n"));
        assertEquals("jdbc:postgresql://localhost:5432/test_clinic", config.getUrl());
        assertEquals("test_user", config.getUsername());
        assertEquals("test-only-value", config.getPassword());
    }

    @Test
    void deployedExamplePasswordFailsBeforeJdbc() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> DatabaseConfig.load(loader("db.url=jdbc:postgresql://localhost:5432/test_clinic\n"
                        + "db.username=test_user\ndb.password=REPLACE_WITH_LOCAL_POSTGRES_PASSWORD\n")));
        assertTrue(exception.getMessage().contains("Replace the example db.password"));
    }

    @Test
    void missingClasspathResourceHasClearError() {
        ClassLoader missing = org.mockito.Mockito.mock(ClassLoader.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> DatabaseConfig.load(missing));
        assertEquals("Database configuration file not found: db.properties", exception.getMessage());
    }

    private ClassLoader loader(String properties) {
        ClassLoader loader = org.mockito.Mockito.mock(ClassLoader.class);
        org.mockito.Mockito.when(loader.getResourceAsStream("db.properties")).thenReturn(
                new java.io.ByteArrayInputStream(properties.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1)));
        return loader;
    }
}
