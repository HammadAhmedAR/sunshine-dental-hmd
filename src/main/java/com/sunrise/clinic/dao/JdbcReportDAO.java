package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public final class JdbcReportDAO implements ReportDAO {
    @Override public List<AppointmentDetails> schedule(Connection c, LocalDate date) throws SQLException {
        return new JdbcAppointmentDAO().list(c, date, null, 0, 0);
    }
    @Override public RevenueSummary revenue(Connection c, LocalDate from, LocalDate to) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("""
                SELECT coalesce(sum(bill_count),0) AS bill_count,
                       coalesce(sum(treatment_revenue),0.00) AS treatment_revenue,
                       coalesce(sum(consultation_revenue),0.00) AS consultation_revenue,
                       coalesce(sum(discounts),0.00) AS discounts,
                       coalesce(sum(total_revenue),0.00) AS total_revenue
                FROM billing_revenue_summary WHERE revenue_date BETWEEN ? AND ?
                """)) {
            s.setObject(1, from); s.setObject(2, to);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return new RevenueSummary(r.getLong("bill_count"), r.getBigDecimal("treatment_revenue"),
                        r.getBigDecimal("consultation_revenue"), r.getBigDecimal("discounts"), r.getBigDecimal("total_revenue"));
            }
        }
    }
}
