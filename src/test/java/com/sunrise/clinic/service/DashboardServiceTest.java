package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.DashboardDAO;
import com.sunrise.clinic.model.DashboardStats;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    @Test void countsUsingSriLankanDayAcrossUtcBoundary() throws Exception {
        Connection connection = mock(Connection.class);
        DashboardDAO dao = mock(DashboardDAO.class);
        Instant now = Instant.parse("2026-09-05T20:00:00Z");
        Instant start = Instant.parse("2026-09-05T18:30:00Z");
        Instant end = Instant.parse("2026-09-06T18:30:00Z");
        when(dao.count(connection, start, end, now)).thenReturn(new DashboardStats(2, 4));
        DashboardService service = new DashboardService(() -> connection, dao, Clock.fixed(now, ZoneOffset.UTC));
        assertEquals(new DashboardStats(2, 4), service.statistics());
        verify(connection).close();
    }
}
