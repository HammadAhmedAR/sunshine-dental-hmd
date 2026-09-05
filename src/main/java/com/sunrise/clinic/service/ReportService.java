package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.ReportDAO;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public final class ReportService {
    private final ConnectionProvider connections;
    private final ReportDAO reports;
    public ReportService(ConnectionProvider connections, ReportDAO reports) {
        this.connections = connections; this.reports = reports;
    }
    public DailySchedule schedule(String dateText) throws SQLException {
        LocalDate date = QueryValidation.date(dateText, true);
        try (Connection c = connections.getConnection()) {
            List<AppointmentDetails> rows = List.copyOf(reports.schedule(c, date));
            String popular = rows.stream().filter(a -> a.status() != AppointmentStatus.CANCELLED && a.status() != AppointmentStatus.NO_SHOW)
                    .collect(Collectors.groupingBy(AppointmentDetails::treatmentName, TreeMap::new, Collectors.counting()))
                    .entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey).findFirst().orElse("No booked treatments");
            return new DailySchedule(rows, rows.stream().filter(a -> a.status() == AppointmentStatus.COMPLETED).count(),
                    rows.stream().filter(a -> a.status() == AppointmentStatus.CANCELLED).count(), popular);
        }
    }
    public RevenueSummary revenue(String fromText, String toText) throws SQLException {
        LocalDate from = QueryValidation.date(fromText, true), to = QueryValidation.date(toText, true);
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 365) {
            throw new ValidationException("Choose an ordered date range of no more than 366 days.");
        }
        try (Connection c = connections.getConnection()) { return reports.revenue(c, from, to); }
    }
}
