package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Appointment;
import org.postgresql.util.PSQLException;
import java.sql.*;
import java.time.Instant;

public final class JdbcAppointmentDAO implements AppointmentDAO {
    @Override
    public boolean hasOverlap(Connection connection, long dentistId, Instant start, Instant end) throws SQLException {
        String sql = """
                SELECT 1 FROM appointments
                WHERE dentist_id = ? AND status IN ('BOOKED', 'COMPLETED')
                  AND starts_at < ? AND ends_at > ? LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dentistId);
            statement.setTimestamp(2, Timestamp.from(end));
            statement.setTimestamp(3, Timestamp.from(start));
            try (ResultSet rows = statement.executeQuery()) { return rows.next(); }
        }
    }

    @Override
    public Appointment insert(Connection connection, long patientId, long dentistId, long treatmentId,
                              long createdBy, Instant start, Instant end) throws SQLException {
        // Omit reference so PostgreSQL generates it. RETURNING retrieves the actual stored reference.
        String sql = """
                INSERT INTO appointments(patient_id, dentist_id, treatment_id, created_by, starts_at, ends_at)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING appointment_id, appointment_number
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);
            statement.setLong(2, dentistId);
            statement.setLong(3, treatmentId);
            statement.setLong(4, createdBy);
            statement.setTimestamp(5, Timestamp.from(start));
            statement.setTimestamp(6, Timestamp.from(end));
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return new Appointment(rows.getLong(1), rows.getString(2), patientId, dentistId,
                        treatmentId, createdBy, start, end);
            }
        } catch (PSQLException exception) {
            if ("23505".equals(exception.getSQLState()) && exception.getServerErrorMessage() != null
                    && "uq_appointments_dentist_start".equals(exception.getServerErrorMessage().getConstraint())) {
                throw new BookingConflictException(exception);
            }
            throw exception;
        }
    }
}
