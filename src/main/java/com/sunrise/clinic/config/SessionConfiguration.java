package com.sunrise.clinic.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SessionConfiguration implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().getSessionCookieConfig().setAttribute("SameSite", "Lax");
        // Enable for an HTTPS deployment; local HTTP development remains possible.
        event.getServletContext().getSessionCookieConfig().setSecure(
                Boolean.parseBoolean(System.getenv("SUNRISE_SECURE_COOKIES")));
    }
}
