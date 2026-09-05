package com.sunrise.clinic.util;

import com.sunrise.clinic.config.DatabaseConfig;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** Optional CLI smoke check, not an automated unit test or web endpoint. */
public final class TestConnection {
    private TestConnection() { }

    public static void main(String[] args) {
        try (Connection connection = new DBConnection(DatabaseConfig.load()).getConnection()) {
            if (!connection.isValid(5)) {
                System.err.println("Database connection validation failed.");
                System.exit(1);
            }
            System.out.println("PostgreSQL connection verified.");
        } catch (SQLException exception) {
            // Driver messages can include connection details. Print only the SQL state.
            System.err.println("Database connection failed. SQL state: " + exception.getSQLState());
            System.exit(1);
        } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
            System.err.println("Database configuration could not be loaded. Check the external file and required settings.");
            System.exit(1);
        }
    }
}
