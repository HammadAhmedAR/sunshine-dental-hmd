package com.sunrise.clinic.util;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/** BCrypt cost 12. Rejects overlong passwords instead of silently truncating them. */
public final class PasswordHasher {
    public boolean acceptable(String password) {
        return password != null && !password.isEmpty() && password.indexOf('\0') < 0
                && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    public String hash(String password) {
        if (!acceptable(password)) throw new IllegalArgumentException("Password must contain 1 to 72 UTF-8 bytes.");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return OpenBSDBCrypt.generate("2b", password.toCharArray(), salt, 12);
    }

    public boolean verify(String password, String hash) {
        if (!acceptable(password) || hash == null) return false;
        try {
            return OpenBSDBCrypt.checkPassword(hash, password.toCharArray());
        } catch (IllegalArgumentException exception) {
            return false; // An unprovisioned or malformed stored hash must never authenticate.
        }
    }
}
