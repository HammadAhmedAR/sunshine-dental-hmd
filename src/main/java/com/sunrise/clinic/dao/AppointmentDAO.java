package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Appointment;
import java.sql.*;
import java.time.Instant;

public interface AppointmentDAO {
    boolean hasOverlap(Connection connection, long dentistId, Instant start, Instant end) throws SQLException;
    Appointment insert(Connection connection, long patientId, long dentistId, long treatmentId,
                       long createdBy, Instant start, Instant end) throws SQLException;
}
