package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public interface BillDAO {
    boolean exists(Connection connection, long appointmentId) throws SQLException;
    BigDecimal treatmentCost(Connection connection, long treatmentId) throws SQLException;
    BigDecimal consultationFee(Connection connection) throws SQLException;
    Bill insert(Connection connection, AppointmentDetails appointment, BillAmounts amounts, long userId, Instant issuedAt) throws SQLException;
    Optional<Bill> find(Connection connection, String number) throws SQLException;
    Optional<Bill> findByAppointment(Connection connection, String reference) throws SQLException;
    List<Bill> history(Connection connection, int limit, int offset) throws SQLException;
}
