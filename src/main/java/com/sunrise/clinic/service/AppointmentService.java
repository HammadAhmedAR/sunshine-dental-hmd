package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeParseException;

public final class AppointmentService {
    private final ConnectionProvider connections;
    private final PatientDAO patients;
    private final DentistDAO dentists;
    private final TreatmentDAO treatments;
    private final AppointmentDAO appointments;
    private final PatientService patientService;
    private final Clock clock;

    public AppointmentService(ConnectionProvider connections, PatientDAO patients, DentistDAO dentists,
                              TreatmentDAO treatments, AppointmentDAO appointments, PatientService patientService, Clock clock) {
        this.connections = connections;
        this.patients = patients;
        this.dentists = dentists;
        this.treatments = treatments;
        this.appointments = appointments;
        this.patientService = patientService;
        this.clock = clock;
    }

    public Appointment register(AppointmentRequest request, long userId) throws SQLException {
        if (request == null) throw new ValidationException("Patient and appointment details are required.");
        long dentistId = requiredId(request.dentistId(), "Select an active dentist.");
        long treatmentId = requiredId(request.treatmentId(), "Select an active treatment.");
        Long existingId = request.existingPatientId() == null || request.existingPatientId().isBlank()
                ? null : requiredId(request.existingPatientId(), "Select a valid patient.");
        PatientDraft newPatient = existingId == null ? patientService.validate(request.patient()) : null;
        Instant start = parseStart(request.date(), request.time());
        if (userId <= 0) throw new ValidationException("A signed-in staff member is required.");
        rejectPast(start);

        try (Connection connection = connections.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            try {
                if (dentists.lockActive(connection, dentistId).isEmpty()) {
                    throw new ValidationException("The selected dentist is unavailable. Choose an active dentist.");
                }
                Treatment treatment = treatments.findActiveById(connection, treatmentId)
                        .orElseThrow(() -> new ValidationException("The selected treatment is unavailable."));
                Instant end = start.plusSeconds(treatment.durationMinutes() * 60L);
                rejectPast(start); // Recheck after waiting for the dentist lock.
                // The conflict rule is introduced after the recorded red test.

                long patientId;
                if (existingId != null) {
                    Patient patient = patients.findById(connection, existingId)
                            .orElseThrow(() -> new ValidationException("The selected patient no longer exists."));
                    patientService.validate(new PatientDraft(patient.fullName(), patient.address(), patient.phone()));
                    patientId = patient.id();
                } else {
                    patientId = patients.insert(connection, newPatient);
                }
                Appointment saved = appointments.insert(connection, patientId, dentistId, treatmentId, userId, start, end);
                connection.commit();
                return saved;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            }
        }
    }

    private long requiredId(String text, String message) {
        try {
            long id = Long.parseLong(text == null ? "" : text);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) { }
        throw new ValidationException(message);
    }

    private Instant parseStart(String date, String time) {
        if (date == null || date.isBlank()) throw new ValidationException("Appointment date is required.");
        if (time == null || time.isBlank()) throw new ValidationException("Appointment time is required.");
        try {
            LocalDate parsedDate = LocalDate.parse(date);
            LocalTime parsedTime = LocalTime.parse(time);
            if (parsedTime.getSecond() != 0 || parsedTime.getNano() != 0
                    || parsedDate.getYear() < 2000 || parsedDate.getYear() > 9999) {
                throw new ValidationException("Enter a valid date and a time in whole minutes.");
            }
            return LocalDateTime.of(parsedDate, parsedTime).atZone(DashboardService.CLINIC_ZONE).toInstant();
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Enter a valid appointment date and time.");
        }
    }

    private void rejectPast(Instant start) {
        if (start.isBefore(clock.instant())) throw new ValidationException("Appointments cannot be booked in the past.");
    }
}
