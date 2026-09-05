package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.Dentist;
import java.sql.*;
import java.util.*;

public final class JdbcDentistDAO implements DentistDAO {
    @Override
    public List<Dentist> findActive(Connection connection) throws SQLException {
        List<Dentist> dentists = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT dentist_id, full_name FROM dentists WHERE is_active = TRUE ORDER BY full_name");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) dentists.add(new Dentist(rows.getLong(1), rows.getString(2)));
        }
        return dentists;
    }
    @Override
    public Optional<Dentist> lockActive(Connection connection, long id) throws SQLException {
        // Serialises registrations for this dentist until commit/rollback.
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT dentist_id, full_name FROM dentists WHERE dentist_id = ? AND is_active = TRUE FOR UPDATE")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(new Dentist(rows.getLong(1), rows.getString(2))) : Optional.empty();
            }
        }
    }
}
