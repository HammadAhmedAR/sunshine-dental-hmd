package com.sunrise.clinic.service;
import com.sunrise.clinic.dao.ReportDAO;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTest {
    Connection c; ReportDAO dao; ReportService service;
    @BeforeEach void setup() throws Exception {
        c = mock(Connection.class); dao = mock(ReportDAO.class);
        ConnectionProvider provider = mock(ConnectionProvider.class);
        when(provider.getConnection()).thenReturn(c);
        service = new ReportService(provider, dao);
    }
    @Test void countsStatusesAndPopularBookedTreatment() throws Exception {
        when(dao.schedule(c, LocalDate.parse("2026-09-05"))).thenReturn(List.of(
            Fixtures.appointment(AppointmentStatus.COMPLETED,false,Fixtures.NOW),
            Fixtures.appointment(AppointmentStatus.CANCELLED,false,Fixtures.NOW),
            Fixtures.appointment(AppointmentStatus.BOOKED,false,Fixtures.NOW)));
        DailySchedule result = service.schedule("2026-09-05");
        assertEquals(3,result.appointments().size());
        assertEquals(1,result.completed()); assertEquals(1,result.cancelled());
        assertEquals("Filling",result.popularTreatment());
    }
    @Test void emptyScheduleHasUsefulSummary() throws Exception {
        when(dao.schedule(c,LocalDate.parse("2026-09-05"))).thenReturn(List.of());
        DailySchedule result = service.schedule("2026-09-05");
        assertEquals(0,result.completed()); assertEquals(0,result.appointments().size());
        assertEquals("No booked treatments",result.popularTreatment());
    }
    @Test void cancelledAndNoShowAreNotPopularBookings() throws Exception {
        when(dao.schedule(c,LocalDate.parse("2026-09-05"))).thenReturn(List.of(
            Fixtures.appointment(AppointmentStatus.CANCELLED,false,Fixtures.NOW),
            Fixtures.appointment(AppointmentStatus.NO_SHOW,false,Fixtures.NOW)));
        assertEquals("No booked treatments",service.schedule("2026-09-05").popularTreatment());
    }
    @Test void rejectsInvalidScheduleDate() {
        assertThrows(ValidationException.class,()->service.schedule("2026-02-30"));
        verifyNoInteractions(dao);
    }
    @Test void rejectsMissingDate() { assertThrows(ValidationException.class,()->service.schedule("")); }
    @Test void rejectsReversedRevenueRange() {
        assertThrows(ValidationException.class,()->service.revenue("2026-09-05","2026-09-04"));
        verifyNoInteractions(dao);
    }
    @Test void rejectsExcessiveRevenueRange() {
        assertThrows(ValidationException.class,()->service.revenue("2025-01-01","2026-09-05"));
    }
    @Test void emptyRevenueIsPassedThroughExactly() throws Exception {
        RevenueSummary zero = new RevenueSummary(0,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);
        when(dao.revenue(c,LocalDate.parse("2026-09-05"),LocalDate.parse("2026-09-05"))).thenReturn(zero);
        assertEquals(zero,service.revenue("2026-09-05","2026-09-05"));
    }
}
