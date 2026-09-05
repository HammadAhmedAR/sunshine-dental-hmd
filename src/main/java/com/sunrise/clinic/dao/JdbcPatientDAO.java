package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.*;
import java.sql.*;
import java.util.*;

public final class JdbcPatientDAO implements PatientDAO {
    private Patient read(ResultSet rows) throws SQLException {
        return new Patient(rows.getLong("patient_id"), rows.getString("full_name"),
                rows.getString("address"), rows.getString("phone"));
    }
    @Override
    public List<Patient> findAll(Connection connection) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT patient_id, full_name, address, phone FROM patients ORDER BY full_name, patient_id");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) patients.add(read(rows));
        }
        return patients;
    }
    @Override
    public Optional<Patient> findById(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT patient_id, full_name, address, phone FROM patients WHERE patient_id = ? FOR SHARE")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(read(rows)) : Optional.empty();
            }
        }
    }
    @Override
    public long insert(Connection connection, PatientDraft patient) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO patients(full_name, address, phone) VALUES (?, ?, ?) RETURNING patient_id")) {
            statement.setString(1, patient.fullName());
            statement.setString(2, patient.address());
            statement.setString(3, patient.phone());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getLong(1);
            }
        }
    }
}
