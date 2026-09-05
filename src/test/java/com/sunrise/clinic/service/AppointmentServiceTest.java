package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {
    private Connection connection;
    private PatientDAO patients;
    private DentistDAO dentists;
    private TreatmentDAO treatments;
    private AppointmentDAO appointments;
    private AppointmentService service;
    private final PatientDraft patient = new PatientDraft("Nimal Perera", "12 Lake Road, Colombo", "0771234567");
    private final Instant start = Instant.parse("2026-09-06T04:30:00Z");
    private final Instant end = start.plusSeconds(1200);

    @BeforeEach void setUp() throws Exception {
        connection = mock(Connection.class);
        patients = mock(PatientDAO.class);
        dentists = mock(DentistDAO.class);
        treatments = mock(TreatmentDAO.class);
        appointments = mock(AppointmentDAO.class);
        when(dentists.lockActive(connection, 1)).thenReturn(Optional.of(new Dentist(1, "Dr. Perera")));
        when(treatments.findActiveById(connection, 2)).thenReturn(Optional.of(new Treatment(2, "Consultation", new BigDecimal("2000.00"), 20)));
        when(patients.insert(eq(connection), any())).thenReturn(5L);
        when(appointments.insert(connection, 5, 1, 2, 3, start, end))
                .thenReturn(new Appointment(8, "APT-2026-00008", 5, 1, 2, 3, start, end));
        service = new AppointmentService(() -> connection, patients, dentists, treatments, appointments,
                new PatientService(null, patients), Clock.fixed(Instant.parse("2026-09-05T00:00:00Z"), ZoneOffset.UTC));
    }

    private AppointmentRequest valid() {
        return new AppointmentRequest("", patient, "1", "2", "2026-09-06", "10:00");
    }

    @Test void rejectsDoubleBooking() throws Exception {
        when(appointments.hasOverlap(connection, 1, start, end)).thenReturn(true);
        assertThrows(ValidationException.class, () -> service.register(valid(), 3));
        verify(patients, never()).insert(any(), any());
        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test void validAppointmentCommitsPatientAndVisitTogether() throws Exception {
        Appointment saved = service.register(valid(), 3);
        assertEquals("APT-2026-00008", saved.appointmentNumber());
        assertEquals(start, saved.startsAt());
        assertEquals(end, saved.endsAt());
        var order = inOrder(connection, dentists, appointments, patients);
        order.verify(connection).setAutoCommit(false);
        order.verify(dentists).lockActive(connection, 1);
        order.verify(appointments).hasOverlap(connection, 1, start, end);
        order.verify(patients).insert(eq(connection), any());
        order.verify(appointments).insert(connection, 5, 1, 2, 3, start, end);
        order.verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test void pastAppointmentRejectedBeforeTransaction() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "1", "2", "2026-09-04", "10:00"), 3));
        verifyNoInteractions(connection);
    }

    @Test void missingPatientRejected() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", null, "1", "2", "2026-09-06", "10:00"), 3));
    }

    @Test void invalidContactRejected() {
        assertThrows(ValidationException.class, () -> service.register(new AppointmentRequest("",
                new PatientDraft("Nimal Perera", "12 Lake Road", "123"), "1", "2", "2026-09-06", "10:00"), 3));
    }

    @Test void missingDentistRejected() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "", "2", "2026-09-06", "10:00"), 3));
    }

    @Test void missingTreatmentRejected() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "1", "", "2026-09-06", "10:00"), 3));
    }

    @Test void missingDateRejected() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "1", "2", null, "10:00"), 3));
    }

    @Test void missingTimeRejected() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "1", "2", "2026-09-06", null), 3));
    }

    @Test void invalidCalendarDateRejected() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "1", "2", "2026-02-30", "10:00"), 3));
    }

    @Test void inactiveDentistRejected() throws Exception {
        when(dentists.lockActive(connection, 1)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> service.register(valid(), 3));
        verify(connection).rollback();
        verify(patients, never()).insert(any(), any());
    }

    @Test void inactiveTreatmentRejected() throws Exception {
        when(treatments.findActiveById(connection, 2)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> service.register(valid(), 3));
        verify(connection).rollback();
    }

    @Test void existingPatientIsReusedWithoutDuplicateInsert() throws Exception {
        when(patients.findById(connection, 5)).thenReturn(Optional.of(new Patient(5, "Nimal Perera", "12 Lake Road", "+94771234567")));
        service.register(new AppointmentRequest("5", null, "1", "2", "2026-09-06", "10:00"), 3);
        verify(patients, never()).insert(any(), any());
        verify(connection).commit();
    }

    @Test void unknownPatientRejected() throws Exception {
        when(patients.findById(connection, 99)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("99", null, "1", "2", "2026-09-06", "10:00"), 3));
        verify(connection).rollback();
    }

    @Test void failedAppointmentInsertRollsBackNewPatient() throws Exception {
        when(appointments.insert(connection, 5, 1, 2, 3, start, end)).thenThrow(new SQLException("test failure"));
        assertThrows(SQLException.class, () -> service.register(valid(), 3));
        verify(patients).insert(eq(connection), any());
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test void failedPatientInsertDoesNotInsertAppointment() throws Exception {
        when(patients.insert(eq(connection), any())).thenThrow(new SQLException("test failure"));
        assertThrows(SQLException.class, () -> service.register(valid(), 3));
        verify(appointments, never()).insert(any(), anyLong(), anyLong(), anyLong(), anyLong(), any(), any());
        verify(connection).rollback();
    }

    @Test void databaseSlotRaceBecomesFriendlyValidationError() throws Exception {
        when(appointments.insert(connection, 5, 1, 2, 3, start, end))
                .thenThrow(new BookingConflictException(new SQLException("duplicate", "23505")));
        assertThrows(ValidationException.class, () -> service.register(valid(), 3));
        verify(connection).rollback();
    }

    @Test void zeroStaffIdRejected() {
        assertThrows(ValidationException.class, () -> service.register(valid(), 0));
    }

    @Test void secondsAreRejectedToKeepMinutePrecision() {
        assertThrows(ValidationException.class, () -> service.register(
                new AppointmentRequest("", patient, "1", "2", "2026-09-06", "10:00:01"), 3));
    }
}
