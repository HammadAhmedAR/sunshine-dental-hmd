package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("pageTitle", "Dashboard");
        req.setAttribute("activePage", "dashboard");
        Object success = req.getSession().getAttribute("success");
        req.getSession().removeAttribute("success");
        req.setAttribute("success", success);
        try {
            req.setAttribute("stats", Services.get(getServletContext()).dashboard().statistics());
        } catch (SQLException exception) {
            getServletContext().log("Dashboard database failure; SQL state: " + exception.getSQLState());
            res.setStatus(503);
            req.setAttribute("error", "Clinic statistics are temporarily unavailable.");
        }
        req.getRequestDispatcher("/WEB-INF/views/dashboard/dashboard.jsp").forward(req, res);
    }
}
