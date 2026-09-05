package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.DashboardStats;
import java.sql.*;
import java.time.Instant;

public final class DashboardDAO {
    public DashboardStats count(Connection connection, Instant dayStart, Instant dayEnd, Instant now) throws SQLException {
        String sql = """
                SELECT count(*) FILTER (WHERE starts_at >= ? AND starts_at < ?) AS today,
                       count(*) FILTER (WHERE starts_at >= ? AND status = 'BOOKED') AS upcoming
                FROM appointments WHERE status IN ('BOOKED', 'COMPLETED')
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(dayStart));
            statement.setTimestamp(2, Timestamp.from(dayEnd));
            statement.setTimestamp(3, Timestamp.from(now));
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return new DashboardStats(rows.getLong("today"), rows.getLong("upcoming"));
            }
        }
    }
}
