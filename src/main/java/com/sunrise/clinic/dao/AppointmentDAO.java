package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Appointment;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import com.sunrise.clinic.model.*;

public interface AppointmentDAO {
    Optional<AppointmentDetails> findByReference(Connection connection, String reference, boolean lock) throws SQLException;
    java.util.List<AppointmentDetails> list(Connection connection, LocalDate date, AppointmentStatus status, int limit, int offset) throws SQLException;
    boolean hasOverlapExcluding(Connection connection, long dentistId, Instant start, Instant end, long excludedId) throws SQLException;
    void reschedule(Connection connection, long id, long dentistId, long treatmentId, Instant start, Instant end) throws SQLException;
    void changeStatus(Connection connection, long id, AppointmentStatus status) throws SQLException;
    boolean hasOverlap(Connection connection, long dentistId, Instant start, Instant end) throws SQLException;
    Appointment insert(Connection connection, long patientId, long dentistId, long treatmentId,
                       long createdBy, Instant start, Instant end) throws SQLException;
}
