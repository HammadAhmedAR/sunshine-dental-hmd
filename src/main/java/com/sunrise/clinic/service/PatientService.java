package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.PatientDAO;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.util.List;

public final class PatientService {
    private final ConnectionProvider connections;
    private final PatientDAO patients;
    public PatientService(ConnectionProvider connections, PatientDAO patients) {
        this.connections = connections;
        this.patients = patients;
    }
    public List<Patient> list() throws SQLException {
        try (Connection connection = connections.getConnection()) { return patients.findAll(connection); }
    }
    public PatientDraft validate(PatientDraft patient) {
        if (patient == null) throw new ValidationException("Patient details are required.");
        String name = patient.fullName() == null ? "" : patient.fullName().trim();
        if (name.length() > 120 || name.codePoints().filter(Character::isLetter).count() < 2
                || !name.matches("[\\p{L}\\p{M} .'-]+")) {
            throw new ValidationException("Enter a patient name using 2 or more letters (maximum 120 characters).");
        }
        String address = patient.address() == null ? "" : patient.address().trim();
        if (address.length() < 3 || address.length() > 300) {
            throw new ValidationException("Enter a patient address between 3 and 300 characters.");
        }
        String phone = patient.phone() == null ? "" : patient.phone().replaceAll("[\\s-]", "");
        if (!phone.matches("(?:0|\\+94)[1-9][0-9]{8}")) {
            throw new ValidationException("Enter a Sri Lankan contact number, for example 0771234567 or +94771234567.");
        }
        // Store one consistent international representation; phone is not a patient identifier.
        if (phone.startsWith("0")) phone = "+94" + phone.substring(1);
        return new PatientDraft(name, address, phone);
    }
}
