package com.sunrise.clinic.util;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

public final class Csrf {
    private Csrf() { }
    public static String token(HttpSession session) {
        synchronized (session) {
            String token = (String) session.getAttribute("csrfToken");
            if (token == null) {
                byte[] bytes = new byte[32];
                new SecureRandom().nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute("csrfToken", token);
            }
            return token;
        }
    }
}
