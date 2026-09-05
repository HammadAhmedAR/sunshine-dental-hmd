package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.*;

public final class AppointmentManagementService {
    private final ConnectionProvider connections;
    private final AppointmentDAO appointments;
    private final DentistDAO dentists;
    private final TreatmentDAO treatments;
    private final Clock clock;
    public AppointmentManagementService(ConnectionProvider connections, AppointmentDAO appointments,
            DentistDAO dentists, TreatmentDAO treatments, Clock clock) {
        this.connections = connections; this.appointments = appointments;
        this.dentists = dentists; this.treatments = treatments; this.clock = clock;
    }
    public void reschedule(String number, String dentist, String treatment, String date, String time) throws SQLException {
        String reference = QueryValidation.reference(number);
        long dentistId = AppointmentValidation.requiredId(dentist, "Select an active dentist.");
        long treatmentId = AppointmentValidation.requiredId(treatment, "Select an active treatment.");
        Instant start = AppointmentValidation.start(date, time);
        AppointmentValidation.notPast(start, clock);
        try (Connection connection = connections.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            try {
                // Same lock order as registration: target dentist before appointment.
                if (dentists.lockActive(connection, dentistId).isEmpty()) throw new ValidationException("Select an active dentist.");
                AppointmentDetails current = appointments.findByReference(connection, reference, true)
                        .orElseThrow(() -> new RecordNotFoundException("Appointment not found."));
                if (current.status() != AppointmentStatus.BOOKED || current.billed()) {
                    throw new ValidationException("Only booked appointments without a bill can be rescheduled.");
                }
                Treatment selected = treatments.findActiveById(connection, treatmentId)
                        .orElseThrow(() -> new ValidationException("Select an active treatment."));
                Instant end = start.plusSeconds(selected.durationMinutes() * 60L);
                AppointmentValidation.notPast(start, clock);
                if (appointments.hasOverlapExcluding(connection, dentistId, start, end, current.id())) {
                    throw new ValidationException("This dentist already has an appointment during that time.");
                }
                appointments.reschedule(connection, current.id(), dentistId, treatmentId, start, end);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                if (exception instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                    throw new ValidationException("This dentist's slot was just booked. Choose another time.");
                }
                throw exception;
            }
        }
    }
    public void changeStatus(String number, String statusText) throws SQLException {
        String reference = QueryValidation.reference(number);
        AppointmentStatus target = QueryValidation.status(statusText);
        if (target == null) throw new ValidationException("Select a status.");
        try (Connection connection = connections.getConnection()) {
            connection.setAutoCommit(false);
            try {
                AppointmentDetails current = appointments.findByReference(connection, reference, true)
                        .orElseThrow(() -> new RecordNotFoundException("Appointment not found."));
                if (current.status() != AppointmentStatus.BOOKED || target == AppointmentStatus.BOOKED) {
                    throw new ValidationException("Only booked appointments can be completed, cancelled or marked no-show.");
                }
                if (current.billed() && target != AppointmentStatus.COMPLETED) {
                    throw new ValidationException("A billed appointment cannot be cancelled or marked no-show.");
                }
                if ((target == AppointmentStatus.COMPLETED || target == AppointmentStatus.NO_SHOW)
                        && current.startsAt().isAfter(clock.instant())) {
                    throw new ValidationException("A future appointment cannot be completed or marked no-show.");
                }
                appointments.changeStatus(connection, current.id(), target);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }
    private void rollback(Connection connection, Exception failure) {
        try { connection.rollback(); } catch (SQLException rollback) { failure.addSuppressed(rollback); }
    }
}
