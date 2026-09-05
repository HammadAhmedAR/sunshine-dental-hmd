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

class BillServiceTest {
    Connection connection;
    AppointmentDAO appointments;
    BillDAO bills;
    BillService service;
    @BeforeEach void setUp() throws Exception {
        connection = mock(Connection.class); appointments = mock(AppointmentDAO.class); bills = mock(BillDAO.class);
        when(appointments.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.COMPLETED, false, Fixtures.NOW)));
        when(bills.treatmentCost(connection, 4)).thenReturn(new BigDecimal("8000.25"));
        when(bills.consultationFee(connection)).thenReturn(new BigDecimal("500.50"));
        service = new BillService(() -> connection, appointments, bills, Clock.fixed(Fixtures.NOW, ZoneOffset.UTC));
    }
    @Test void addsTreatmentAndConsultationExactly() {
        assertEquals(new BigDecimal("8500.75"), BillService.calculate(new BigDecimal("8000.25"), new BigDecimal("500.50")).total());
    }
    @Test void decimalPrecisionIsPreserved() {
        assertEquals(new BigDecimal("0.30"), BillService.calculate(new BigDecimal("0.10"), new BigDecimal("0.20")).total());
    }
    @Test void negativeTreatmentRejected() {
        assertThrows(ValidationException.class, () -> BillService.calculate(new BigDecimal("-1"), BigDecimal.ZERO));
    }
    @Test void missingConsultationSettingRejected() {
        assertThrows(ValidationException.class, () -> BillService.calculate(BigDecimal.ONE, null));
    }
    @Test void extraFractionalPrecisionRejected() {
        assertThrows(ValidationException.class, () -> BillService.calculate(new BigDecimal("1.001"), BigDecimal.ZERO));
    }
    @Test void overflowingTotalRejected() {
        assertThrows(ValidationException.class, () -> BillService.calculate(new BigDecimal("9999999999.99"), BigDecimal.ONE));
    }
    @Test void cancelledAppointmentCannotBeBilled() throws Exception {
        when(appointments.findByReference(connection, "APT-2026-00001", true))
                .thenReturn(Optional.of(Fixtures.appointment(AppointmentStatus.CANCELLED, false, Fixtures.NOW)));
        assertThrows(ValidationException.class, () -> service.create("APT-2026-00001", 1));
        verify(bills, never()).insert(any(), any(), any(), anyLong(), any());
    }
    @Test void duplicateBillRejectedAndRolledBack() throws Exception {
        when(bills.exists(connection, 1)).thenReturn(true);
        assertThrows(ValidationException.class, () -> service.create("APT-2026-00001", 1));
        verify(connection).rollback(); verify(connection, never()).commit();
    }
    @Test void nonexistentAppointmentRejected() throws Exception {
        when(appointments.findByReference(connection, "APT-2026-00001", true)).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> service.create("APT-2026-00001", 1));
    }
    @Test void finalBillUsesServerValuesAndCommits() throws Exception {
        service.create("APT-2026-00001", 1);
        verify(bills).insert(eq(connection), any(), eq(new BillAmounts(new BigDecimal("8000.25"), new BigDecimal("500.50"), new BigDecimal("8500.75"))), eq(1L), eq(Fixtures.NOW));
        verify(connection).commit();
    }
    @Test void failedInsertRollsBack() throws Exception {
        when(bills.insert(any(), any(), any(), anyLong(), any())).thenThrow(new SQLException("failure"));
        assertThrows(SQLException.class, () -> service.create("APT-2026-00001", 1));
        verify(connection).rollback();
    }
}
