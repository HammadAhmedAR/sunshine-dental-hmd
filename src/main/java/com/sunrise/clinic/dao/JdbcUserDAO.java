package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.User;
import com.sunrise.clinic.util.ConnectionProvider;
import java.sql.*;
import java.util.Optional;

public final class JdbcUserDAO implements UserDAO {
    private final ConnectionProvider connections;
    public JdbcUserDAO(ConnectionProvider connections) { this.connections = connections; }

    @Override
    public Optional<Credentials> findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, display_name, role, password_hash, is_active FROM users WHERE username = ?";
        try (Connection connection = connections.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) return Optional.empty();
                User user = new User(rows.getLong("user_id"), rows.getString("username"),
                        rows.getString("display_name"), rows.getString("role"));
                return Optional.of(new Credentials(user, rows.getString("password_hash"), rows.getBoolean("is_active")));
            }
        }
    }
}
