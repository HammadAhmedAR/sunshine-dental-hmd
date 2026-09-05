package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class JdbcBillDAO implements BillDAO {
    private static final String SELECT = "SELECT b.*, a.appointment_number FROM bills b JOIN appointments a ON a.appointment_id=b.appointment_id ";
    private Bill read(ResultSet rows) throws SQLException {
        return new Bill(rows.getLong("bill_id"), rows.getString("bill_number"), rows.getString("appointment_number"),
                rows.getString("patient_name"), rows.getString("dentist_name"), rows.getString("treatment_name"),
                rows.getBigDecimal("treatment_cost"), rows.getBigDecimal("consultation_fee"), rows.getBigDecimal("discount"),
                rows.getBigDecimal("total"), rows.getTimestamp("issued_at").toInstant());
    }
    @Override public boolean exists(Connection c, long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM bills WHERE appointment_id=?")) {
            s.setLong(1, id); try (ResultSet r = s.executeQuery()) { return r.next(); }
        }
    }
    @Override public BigDecimal treatmentCost(Connection c, long id) throws SQLException {
        // Retired treatments on existing visits can still be billed. Lock price while creating the bill.
        try (PreparedStatement s = c.prepareStatement("SELECT price FROM treatments WHERE treatment_id=? FOR SHARE")) {
            s.setLong(1, id); try (ResultSet r = s.executeQuery()) { return r.next() ? r.getBigDecimal(1) : null; }
        }
    }
    @Override public BigDecimal consultationFee(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT monetary_value FROM clinic_settings WHERE setting_key='consultation_fee' FOR SHARE");
             ResultSet r = s.executeQuery()) { return r.next() ? r.getBigDecimal(1) : null; }
    }
    @Override public Bill insert(Connection c, AppointmentDetails a, BillAmounts amounts, long userId, Instant now) throws SQLException {
        long sequence;
        try (PreparedStatement s = c.prepareStatement("SELECT nextval('bill_reference_seq')"); ResultSet r = s.executeQuery()) {
            r.next(); sequence = r.getLong(1);
        }
        String number = String.format(Locale.ROOT, "BILL-%04d-%05d", now.atZone(ZoneId.of("Asia/Colombo")).getYear(), sequence);
        try (PreparedStatement s = c.prepareStatement("""
                INSERT INTO bills(appointment_id,issued_by,subtotal,discount,bill_number,treatment_cost,consultation_fee,
                                  patient_name,dentist_name,treatment_name,issued_at)
                VALUES (?,?,?,0,?,?,?,?,?,?,?) RETURNING bill_id,total
                """)) {
            s.setLong(1, a.id()); s.setLong(2, userId); s.setBigDecimal(3, amounts.total()); s.setString(4, number);
            s.setBigDecimal(5, amounts.treatmentCost()); s.setBigDecimal(6, amounts.consultationFee());
            s.setString(7, a.patientName()); s.setString(8, a.dentistName()); s.setString(9, a.treatmentName());
            s.setTimestamp(10, Timestamp.from(now));
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return new Bill(r.getLong(1), number, a.appointmentNumber(), a.patientName(), a.dentistName(), a.treatmentName(),
                        amounts.treatmentCost(), amounts.consultationFee(), new BigDecimal("0.00"), r.getBigDecimal(2), now);
            }
        }
    }
    @Override public Optional<Bill> find(Connection c, String number) throws SQLException {
        return one(c, SELECT + "WHERE b.bill_number=?", number);
    }
    @Override public Optional<Bill> findByAppointment(Connection c, String number) throws SQLException {
        return one(c, SELECT + "WHERE a.appointment_number=?", number);
    }
    private Optional<Bill> one(Connection c, String sql, String value) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, value); try (ResultSet r = s.executeQuery()) { return r.next() ? Optional.of(read(r)) : Optional.empty(); }
        }
    }
    @Override public List<Bill> history(Connection c, int limit, int offset) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(SELECT + "ORDER BY b.issued_at DESC,b.bill_id DESC LIMIT ? OFFSET ?")) {
            s.setInt(1, limit); s.setInt(2, offset);
            List<Bill> result = new ArrayList<>();
            try (ResultSet r = s.executeQuery()) { while (r.next()) result.add(read(r)); } return result;
        }
    }
}
