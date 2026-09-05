package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.AppointmentDAO;
import com.sunrise.clinic.model.*;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentQueryServiceTest {
    private final Connection connection = mock(Connection.class);
    private final AppointmentDAO dao = mock(AppointmentDAO.class);
    private final AppointmentQueryService service = new AppointmentQueryService(() -> connection, dao);
    @Test void knownReferenceIsTrimmedAndFound() throws Exception {
        var appointment = Fixtures.appointment(AppointmentStatus.BOOKED, false, Fixtures.NOW);
        when(dao.findByReference(connection, "APT-2026-00001", false)).thenReturn(Optional.of(appointment));
        assertEquals(appointment, service.find("  APT-2026-00001  "));
    }
    @Test void legacyReferenceIsAccepted() throws Exception {
        when(dao.findByReference(connection, "SDC-1", false)).thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.BOOKED, false, Fixtures.NOW)));
        assertNotNull(service.find("SDC-1"));
    }
    @Test void unknownReferenceIsNotFound() {
        assertThrows(RecordNotFoundException.class, () -> service.find("APT-2026-99999"));
    }
    @Test void blankReferenceRejectedBeforeDatabase() {
        assertThrows(ValidationException.class, () -> service.find(" "));
        verifyNoInteractions(connection, dao);
    }
    @Test void injectedReferenceRejectedBeforeDatabase() {
        assertThrows(ValidationException.class, () -> service.find("' OR 1=1 --"));
        verifyNoInteractions(dao);
    }
    @Test void paginationUsesBoundedDatabaseQuery() throws Exception {
        var appointment = Fixtures.appointment(AppointmentStatus.BOOKED, false, Fixtures.NOW);
        when(dao.list(connection, null, null, 21, 20)).thenReturn(Collections.nCopies(21, appointment));
        AppointmentPage page = service.list("", "", "2");
        assertEquals(20, page.appointments().size()); assertTrue(page.hasNext());
    }
}
