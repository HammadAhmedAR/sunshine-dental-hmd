package com.sunrise.clinic.dao;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JdbcAppointmentDAOTest {
    @Test void combinesDatabaseSequenceWithSriLankanVisitYear() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement sequence = mock(PreparedStatement.class);
        PreparedStatement insert = mock(PreparedStatement.class);
        ResultSet sequenceRows = mock(ResultSet.class);
        ResultSet insertedRows = mock(ResultSet.class);
        when(connection.prepareStatement("SELECT nextval('appointment_reference_seq')")).thenReturn(sequence);
        when(connection.prepareStatement(startsWith("INSERT INTO appointments"))).thenReturn(insert);
        when(sequence.executeQuery()).thenReturn(sequenceRows);
        when(sequenceRows.getLong(1)).thenReturn(42L);
        when(insert.executeQuery()).thenReturn(insertedRows);
        when(insertedRows.getLong(1)).thenReturn(10L);
        when(insertedRows.getString(2)).thenReturn("APT-2027-00042");
        Instant start = Instant.parse("2026-12-31T20:00:00Z");

        var saved = new JdbcAppointmentDAO().insert(connection, 1, 2, 3, 4, start, start.plusSeconds(1200));

        verify(insert).setString(7, "APT-2027-00042");
        assertEquals("APT-2027-00042", saved.appointmentNumber());
        verify(sequence).close();
        verify(insert).close();
        verify(connection, never()).commit(); // Transaction ownership stays in the service.
    }
}
