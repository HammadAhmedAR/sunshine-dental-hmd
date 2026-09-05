package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.*;

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
        long dentistId = AppointmentValidation.requiredId(request.dentistId(), "Select an active dentist.");
        long treatmentId = AppointmentValidation.requiredId(request.treatmentId(), "Select an active treatment.");
        Long existingId = request.existingPatientId() == null || request.existingPatientId().isBlank()
                ? null : AppointmentValidation.requiredId(request.existingPatientId(), "Select a valid patient.");
        PatientDraft newPatient = existingId == null ? patientService.validate(request.patient()) : null;
        Instant start = AppointmentValidation.start(request.date(), request.time());
        if (userId <= 0) throw new ValidationException("A signed-in staff member is required.");
        AppointmentValidation.notPast(start, clock);

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
                AppointmentValidation.notPast(start, clock); // Recheck after waiting for the dentist lock.
                if (appointments.hasOverlap(connection, dentistId, start, end)) {
                    throw new ValidationException("This dentist already has an appointment during that time. Choose another slot.");
                }

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
                if (exception instanceof BookingConflictException) {
                    throw new ValidationException("This dentist's slot was just booked. Choose another time.");
                }
                throw exception;
            }
        }
    }

}
