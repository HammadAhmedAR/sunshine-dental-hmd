package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.UserDAO;
import com.sunrise.clinic.model.User;
import com.sunrise.clinic.util.PasswordHasher;
import java.sql.SQLException;
import java.util.Optional;

public final class AuthService {
    private final UserDAO users;
    private final PasswordHasher passwords;
    private final String dummyHash;

    public AuthService(UserDAO users, PasswordHasher passwords) {
        this.users = users;
        this.passwords = passwords;
        this.dummyHash = passwords.hash("Timing-only-placeholder");
    }

    public Optional<User> authenticate(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || username.length() > 50 || !passwords.acceptable(password)) {
            return Optional.empty();
        }
        Optional<UserDAO.Credentials> found = users.findByUsername(username.trim());
        String hash = found.map(UserDAO.Credentials::passwordHash).orElse(dummyHash);
        boolean matches = passwords.verify(password, hash);
        return found.filter(account -> matches && account.active()).map(UserDAO.Credentials::user);
    }
}
