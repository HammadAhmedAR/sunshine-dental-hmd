package com.sunrise.clinic.dao;

import com.sunrise.clinic.model.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    record Credentials(User user, String passwordHash, boolean active) { }
    Optional<Credentials> findByUsername(String username) throws SQLException;
}
