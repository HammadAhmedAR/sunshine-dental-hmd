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
                .thenReturn(new Appointment(8, "SDC-8", 5, 1, 2, 3, start, end));
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
}
