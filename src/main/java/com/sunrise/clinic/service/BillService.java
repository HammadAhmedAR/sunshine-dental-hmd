package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.*;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.time.Clock;
import java.math.*;
import java.util.*;

public final class BillService {
    private final ConnectionProvider connections;
    private final AppointmentDAO appointments;
    private final BillDAO bills;
    private final Clock clock;
    public BillService(ConnectionProvider connections, AppointmentDAO appointments, BillDAO bills, Clock clock) {
        this.connections = connections; this.appointments = appointments; this.bills = bills; this.clock = clock;
    }
    public static BillAmounts calculate(BigDecimal treatment, BigDecimal consultation) {
        BigDecimal cost = money(treatment, "Treatment cost");
        BigDecimal fee = money(consultation, "Consultation fee");
        return new BillAmounts(cost, fee, money(cost.add(fee), "Total"));
    }
    private static BigDecimal money(BigDecimal value, String field) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new ValidationException(field + " must be a valid non-negative LKR amount.");
        }
        try { return value.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException e) { throw new ValidationException(field + " must have at most two decimal places."); }
    }
    private void billable(AppointmentDetails appointment) {
        if (appointment.status() != AppointmentStatus.BOOKED && appointment.status() != AppointmentStatus.COMPLETED) {
            throw new ValidationException("Cancelled and no-show appointments cannot be billed.");
        }
    }
    public BillPreview preview(String number) throws SQLException {
        String reference = QueryValidation.reference(number);
        try (Connection connection = connections.getConnection()) {
            AppointmentDetails a = appointments.findByReference(connection, reference, false)
                    .orElseThrow(() -> new RecordNotFoundException("Appointment not found."));
            billable(a);
            if (a.billed()) throw new ValidationException("A final bill already exists for this appointment.");
            return new BillPreview(a, calculate(bills.treatmentCost(connection, a.treatmentId()), bills.consultationFee(connection)));
        }
    }
    public Bill create(String number, long userId) throws SQLException {
        String reference = QueryValidation.reference(number);
        if (userId <= 0) throw new ValidationException("A signed-in staff member is required.");
        try (Connection connection = connections.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            try {
                AppointmentDetails a = appointments.findByReference(connection, reference, true)
                        .orElseThrow(() -> new RecordNotFoundException("Appointment not found."));
                billable(a);
                if (bills.exists(connection, a.id())) throw new ValidationException("A final bill already exists for this appointment.");
                BillAmounts amounts = calculate(bills.treatmentCost(connection, a.treatmentId()), bills.consultationFee(connection));
                Bill bill = bills.insert(connection, a, amounts, userId, clock.instant());
                connection.commit(); return bill;
            } catch (SQLException | RuntimeException failure) {
                try { connection.rollback(); } catch (SQLException rollback) { failure.addSuppressed(rollback); }
                if (failure instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                    throw new ValidationException("A final bill already exists for this appointment.");
                }
                throw failure;
            }
        }
    }
    public Bill receipt(String billNumber, String appointmentNumber) throws SQLException {
        if (appointmentNumber != null && !appointmentNumber.isBlank()) {
            String reference = QueryValidation.reference(appointmentNumber);
            try (Connection connection = connections.getConnection()) {
                return bills.findByAppointment(connection, reference).orElseThrow(() -> new RecordNotFoundException("Bill not found."));
            }
        }
        String number = billNumber == null ? "" : billNumber.trim();
        if (number.length() > 40 || !number.matches("BILL-(?:[0-9]{4}-[0-9]{5,}|LEGACY-[0-9]+)")) {
            throw new ValidationException("Enter a valid bill number.");
        }
        try (Connection connection = connections.getConnection()) {
            return bills.find(connection, number).orElseThrow(() -> new RecordNotFoundException("Bill not found."));
        }
    }
    public BillPage history(String pageText) throws SQLException {
        int page = QueryValidation.page(pageText);
        try (Connection connection = connections.getConnection()) {
            List<Bill> result = bills.history(connection, 21, (page - 1) * 20);
            return new BillPage(List.copyOf(result.subList(0, Math.min(20, result.size()))), page, result.size() > 20);
        }
    }
}
