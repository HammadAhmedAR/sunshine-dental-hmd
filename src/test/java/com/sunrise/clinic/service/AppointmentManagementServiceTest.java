package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentManagementServiceTest {
    Connection connection;
    AppointmentDAO dao;
    DentistDAO dentists;
    TreatmentDAO treatments;
    AppointmentManagementService service;
    @BeforeEach void setUp() throws Exception {
        connection = mock(Connection.class); dao = mock(AppointmentDAO.class);
        dentists = mock(DentistDAO.class); treatments = mock(TreatmentDAO.class);
        when(dentists.lockActive(connection, 3)).thenReturn(Optional.of(new Dentist(3, "Dr. Perera")));
        when(treatments.findActiveById(connection, 4)).thenReturn(Optional.of(new Treatment(4, "Filling", new BigDecimal("8000"), 20)));
        when(dao.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.BOOKED, false, Fixtures.NOW)));
        service = new AppointmentManagementService(() -> connection, dao, dentists, treatments, Clock.fixed(Fixtures.NOW, ZoneOffset.UTC));
    }
    @Test void validRescheduleExcludesCurrentAppointment() throws Exception {
        Instant start = Instant.parse("2026-09-06T04:30:00Z");
        service.reschedule("APT-2026-00001", "3", "4", "2026-09-06", "10:00");
        verify(dao).hasOverlapExcluding(connection, 3, start, start.plusSeconds(1200), 1);
        verify(dao, never()).hasOverlap(any(), anyLong(), any(), any());
        verify(dao).reschedule(connection, 1, 3, 4, start, start.plusSeconds(1200));
        verify(connection).commit();
    }
    @Test void pastRescheduleRejected() {
        assertThrows(ValidationException.class, () -> service.reschedule("APT-2026-00001", "3", "4", "2026-09-04", "10:00"));
        verifyNoInteractions(connection);
    }
    @Test void otherAppointmentConflictRejected() throws Exception {
        when(dao.hasOverlapExcluding(eq(connection), eq(3L), any(), any(), eq(1L))).thenReturn(true);
        assertThrows(ValidationException.class, () -> service.reschedule("APT-2026-00001", "3", "4", "2026-09-06", "10:00"));
        verify(connection).rollback(); verify(connection, never()).commit();
    }
    @Test void billedAppointmentCannotBeEdited() throws Exception {
        when(dao.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.BOOKED, true, Fixtures.NOW)));
        assertThrows(ValidationException.class, () -> service.reschedule("APT-2026-00001", "3", "4", "2026-09-06", "10:00"));
    }
    @Test void cancellationUpdatesStatusWithoutDeleting() throws Exception {
        service.changeStatus("APT-2026-00001", "CANCELLED");
        verify(dao).changeStatus(connection, 1, AppointmentStatus.CANCELLED); verify(connection).commit();
    }
    @Test void bookedVisitCanBeCompletedAtItsStart() throws Exception {
        service.changeStatus("APT-2026-00001", "COMPLETED");
        verify(dao).changeStatus(connection, 1, AppointmentStatus.COMPLETED);
    }
    @Test void terminalStatusCannotBeReopened() throws Exception {
        when(dao.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.CANCELLED, false, Fixtures.NOW)));
        assertThrows(ValidationException.class, () -> service.changeStatus("APT-2026-00001", "BOOKED"));
    }
    @Test void futureVisitCannotBeCompleted() throws Exception {
        when(dao.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.BOOKED, false, Fixtures.NOW.plusSeconds(60))));
        assertThrows(ValidationException.class, () -> service.changeStatus("APT-2026-00001", "COMPLETED"));
    }
    @Test void billedVisitCannotBeCancelled() throws Exception {
        when(dao.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.BOOKED, true, Fixtures.NOW)));
        assertThrows(ValidationException.class, () -> service.changeStatus("APT-2026-00001", "CANCELLED"));
    }
}
