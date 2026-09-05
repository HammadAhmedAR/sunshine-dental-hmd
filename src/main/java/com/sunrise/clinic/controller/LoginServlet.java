package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.model.User;
import com.sunrise.clinic.util.Csrf;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (req.getSession().getAttribute("loggedUser") instanceof User) {
            res.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Optional<User> user = Services.get(getServletContext()).auth()
                    .authenticate(req.getParameter("username"), req.getParameter("password"));
            if (user.isPresent()) {
                req.getSession().invalidate();
                HttpSession session = req.getSession(true);
                session.setAttribute("loggedUser", user.get());
                Csrf.token(session);
                res.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }
            req.setAttribute("error", "The username or password is incorrect.");
        } catch (SQLException exception) {
            getServletContext().log("Login database failure; SQL state: " + exception.getSQLState());
            Throwable cause = exception.getCause();
            if (cause != null) {
                // Log only a known configuration reason, never arbitrary JDBC messages or credentials.
                String reason = "Replace the example db.password before connecting.".equals(cause.getMessage())
                        ? "Example database password must be replaced in the deployed configuration."
                        : "Inspect the deployed database configuration and server connection settings.";
                getServletContext().log("Login failure cause: " + cause.getClass().getName() + "; " + reason);
            }
            res.setStatus(503);
            req.setAttribute("error", "Sign-in is temporarily unavailable. Please try again shortly.");
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, res);
    }
}
