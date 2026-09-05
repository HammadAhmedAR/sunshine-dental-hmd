package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.AppointmentNumberGenerator;
import org.postgresql.util.PSQLException;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.*;

public final class JdbcAppointmentDAO implements AppointmentDAO {
    private static final String DETAILS = """
            SELECT a.*, p.full_name AS patient_name, p.address, p.phone,
                   d.full_name AS dentist_name, t.name AS treatment_name, t.price,
                   (b.bill_id IS NOT NULL) AS billed
            FROM appointments a JOIN patients p ON p.patient_id = a.patient_id
            JOIN dentists d ON d.dentist_id = a.dentist_id
            JOIN treatments t ON t.treatment_id = a.treatment_id
            LEFT JOIN bills b ON b.appointment_id = a.appointment_id
            """;

    private AppointmentDetails read(ResultSet rows) throws SQLException {
        return new AppointmentDetails(rows.getLong("appointment_id"), rows.getString("appointment_number"),
                rows.getLong("patient_id"), rows.getString("patient_name"), rows.getString("address"), rows.getString("phone"),
                rows.getLong("dentist_id"), rows.getString("dentist_name"), rows.getLong("treatment_id"), rows.getString("treatment_name"),
                rows.getBigDecimal("price"), rows.getTimestamp("starts_at").toInstant(), rows.getTimestamp("ends_at").toInstant(),
                AppointmentStatus.valueOf(rows.getString("status")), rows.getTimestamp("created_at").toInstant(), rows.getBoolean("billed"));
    }

    @Override
    public Optional<AppointmentDetails> findByReference(Connection connection, String reference, boolean lock) throws SQLException {
        if (lock) {
            // Read joined bill state in a fresh READ_COMMITTED snapshot after any lock wait.
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT appointment_id FROM appointments WHERE appointment_number=? FOR UPDATE")) {
                statement.setString(1, reference);
                try (ResultSet rows = statement.executeQuery()) { if (!rows.next()) return Optional.empty(); }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(DETAILS + " WHERE a.appointment_number = ?")) {
            statement.setString(1, reference);
            try (ResultSet rows = statement.executeQuery()) { return rows.next() ? Optional.of(read(rows)) : Optional.empty(); }
        }
    }

    @Override
    public List<AppointmentDetails> list(Connection connection, LocalDate date, AppointmentStatus status, int limit, int offset) throws SQLException {
        // Only fixed SQL fragments are combined. All user-supplied values are bound parameters.
        String sql = DETAILS + " WHERE 1=1" + (date == null ? "" : " AND a.starts_at >= ? AND a.starts_at < ?")
                + (status == null ? "" : " AND a.status = ?") + " ORDER BY a.starts_at, a.appointment_id"
                + (limit > 0 ? " LIMIT ? OFFSET ?" : "");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameter = 1;
            if (date != null) {
                statement.setTimestamp(parameter++, Timestamp.from(date.atStartOfDay(ZoneId.of("Asia/Colombo")).toInstant()));
                statement.setTimestamp(parameter++, Timestamp.from(date.plusDays(1).atStartOfDay(ZoneId.of("Asia/Colombo")).toInstant()));
            }
            if (status != null) statement.setString(parameter++, status.name());
            if (limit > 0) { statement.setInt(parameter++, limit); statement.setInt(parameter, offset); }
            List<AppointmentDetails> result = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) { while (rows.next()) result.add(read(rows)); }
            return result;
        }
    }

    @Override
    public boolean hasOverlapExcluding(Connection connection, long dentistId, Instant start, Instant end, long excludedId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT 1 FROM appointments WHERE dentist_id = ? AND status IN ('BOOKED','COMPLETED')
                AND starts_at < ? AND ends_at > ? AND appointment_id <> ? LIMIT 1
                """)) {
            statement.setLong(1, dentistId);
            statement.setTimestamp(2, Timestamp.from(end));
            statement.setTimestamp(3, Timestamp.from(start));
            statement.setLong(4, excludedId);
            try (ResultSet rows = statement.executeQuery()) { return rows.next(); }
        }
    }

    @Override
    public void reschedule(Connection connection, long id, long dentistId, long treatmentId, Instant start, Instant end) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE appointments SET dentist_id=?, treatment_id=?, starts_at=?, ends_at=? WHERE appointment_id=?
                """)) {
            statement.setLong(1, dentistId); statement.setLong(2, treatmentId);
            statement.setTimestamp(3, Timestamp.from(start)); statement.setTimestamp(4, Timestamp.from(end));
            statement.setLong(5, id); statement.executeUpdate();
        }
    }

    @Override
    public void changeStatus(Connection connection, long id, AppointmentStatus status) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE appointments SET status=? WHERE appointment_id=?")) {
            statement.setString(1, status.name()); statement.setLong(2, id); statement.executeUpdate();
        }
    }
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
        long sequence;
        try (PreparedStatement next = connection.prepareStatement("SELECT nextval('appointment_reference_seq')");
             ResultSet rows = next.executeQuery()) {
            rows.next();
            sequence = rows.getLong(1);
        }
        String reference = AppointmentNumberGenerator.generate(
                start.atZone(ZoneId.of("Asia/Colombo")).toLocalDate(), sequence);
        String sql = """
                INSERT INTO appointments(patient_id, dentist_id, treatment_id, created_by, starts_at, ends_at, appointment_number)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING appointment_id, appointment_number
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, patientId);
            statement.setLong(2, dentistId);
            statement.setLong(3, treatmentId);
            statement.setLong(4, createdBy);
            statement.setTimestamp(5, Timestamp.from(start));
            statement.setTimestamp(6, Timestamp.from(end));
            statement.setString(7, reference);
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
