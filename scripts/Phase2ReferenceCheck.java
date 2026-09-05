import com.sunrise.clinic.config.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.*;
import java.sql.*;
import java.time.*;

/** Manual check on the isolated fixture after migration 003; creates fictional test data. */
class Phase2ReferenceCheck {
    public static void main(String[] args) throws Exception {
        DatabaseConfig config = DatabaseConfig.load();
        if (!config.getUrl().equals("jdbc:postgresql://127.0.0.1:55432/sunrise_dental_clinic")) {
            throw new IllegalArgumentException("This check requires the isolated test fixture.");
        }
        DBConnection connections = new DBConnection(config);
        long legacyBefore = legacyCount(connections);
        if (legacyBefore == 0) throw new AssertionError("Expected retained legacy references in this fixture");
        LocalDate date = LocalDate.now(ZoneId.of("Asia/Colombo")).plusDays(5);
        AppointmentRequest request = new AppointmentRequest("",
                new PatientDraft("Reference Check Patient", "12 Test Road", "0771234567"),
                "1", "1", date.toString(), "14:00");
        Appointment saved = new Services().appointments().register(request, 1);
        if (!saved.appointmentNumber().matches("APT-" + date.getYear() + "-[0-9]{5,}")) {
            throw new AssertionError("Wrong reference: " + saved.appointmentNumber());
        }
        System.out.println("PASS: Real registration saved " + saved.appointmentNumber());
        if (legacyBefore != legacyCount(connections)) throw new AssertionError("Legacy references changed");
        System.out.println("PASS: Existing SDC references are retained after migration and registration");
    }

    private static long legacyCount(DBConnection connections) throws SQLException {
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT count(*) FROM appointments WHERE appointment_number LIKE 'SDC-%'");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }
}
