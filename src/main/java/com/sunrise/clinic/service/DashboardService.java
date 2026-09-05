package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.DashboardDAO;
import com.sunrise.clinic.model.DashboardStats;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.*;

public final class DashboardService {
    public static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Colombo");
    private final ConnectionProvider connections;
    private final DashboardDAO dashboard;
    private final Clock clock;
    public DashboardService(ConnectionProvider connections, DashboardDAO dashboard, Clock clock) {
        this.connections = connections; this.dashboard = dashboard; this.clock = clock;
    }
    public DashboardStats statistics() throws SQLException {
        Instant now = clock.instant();
        LocalDate today = now.atZone(CLINIC_ZONE).toLocalDate();
        try (Connection connection = connections.getConnection()) {
            return dashboard.count(connection, today.atStartOfDay(CLINIC_ZONE).toInstant(),
                    today.plusDays(1).atStartOfDay(CLINIC_ZONE).toInstant(), now);
        }
    }
}
