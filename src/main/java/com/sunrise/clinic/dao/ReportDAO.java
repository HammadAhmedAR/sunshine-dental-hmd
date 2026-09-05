package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public interface ReportDAO {
    List<AppointmentDetails> schedule(Connection connection, LocalDate date) throws SQLException;
    RevenueSummary revenue(Connection connection, LocalDate from, LocalDate to) throws SQLException;
}
