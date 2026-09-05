package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.AppointmentDAO;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public final class AppointmentQueryService {
    public static final int PAGE_SIZE = 20;
    private final ConnectionProvider connections;
    private final AppointmentDAO appointments;
    public AppointmentQueryService(ConnectionProvider connections, AppointmentDAO appointments) {
        this.connections = connections; this.appointments = appointments;
    }
    public AppointmentDetails find(String reference) throws SQLException {
        String valid = QueryValidation.reference(reference);
        try (Connection connection = connections.getConnection()) {
            return appointments.findByReference(connection, valid, false)
                    .orElseThrow(() -> new RecordNotFoundException("No appointment was found with that number."));
        }
    }
    public AppointmentPage list(String date, String status, String pageText) throws SQLException {
        LocalDate day = QueryValidation.date(date, false);
        AppointmentStatus filter = QueryValidation.status(status);
        int page = QueryValidation.page(pageText);
        try (Connection connection = connections.getConnection()) {
            List<AppointmentDetails> rows = appointments.list(connection, day, filter, PAGE_SIZE + 1, (page - 1) * PAGE_SIZE);
            return new AppointmentPage(List.copyOf(rows.subList(0, Math.min(rows.size(), PAGE_SIZE))), page, rows.size() > PAGE_SIZE);
        }
    }
}
