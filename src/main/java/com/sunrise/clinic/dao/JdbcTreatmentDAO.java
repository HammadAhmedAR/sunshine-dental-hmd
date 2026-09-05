package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Treatment;
import java.sql.*;
import java.util.*;

public final class JdbcTreatmentDAO implements TreatmentDAO {
    private Treatment read(ResultSet rows) throws SQLException {
        return new Treatment(rows.getLong("treatment_id"), rows.getString("name"),
                rows.getBigDecimal("price"), rows.getInt("duration_minutes"));
    }
    @Override
    public List<Treatment> findActive(Connection connection) throws SQLException {
        List<Treatment> treatments = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT treatment_id, name, price, duration_minutes FROM treatments WHERE is_active = TRUE ORDER BY name");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) treatments.add(read(rows));
        }
        return treatments;
    }
    @Override
    public Optional<Treatment> findActiveById(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT treatment_id, name, price, duration_minutes FROM treatments WHERE treatment_id = ? AND is_active = TRUE FOR SHARE")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(read(rows)) : Optional.empty();
            }
        }
    }
}
