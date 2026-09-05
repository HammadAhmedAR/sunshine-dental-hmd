package com.sunrise.clinic.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

final class ViewSupport {
    private ViewSupport() { }
    static void page(HttpServletRequest req, HttpServletResponse res, String view, String title, String active)
            throws ServletException, IOException {
        req.setAttribute("pageTitle", title); req.setAttribute("activePage", active);
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object success = session.getAttribute("success");
            session.removeAttribute("success");
            req.setAttribute("success", success);
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, res);
    }
    static void error(HttpServletRequest req, HttpServletResponse res, int status, String message)
            throws ServletException, IOException {
        res.setStatus(status); req.setAttribute("error", message);
        page(req, res, "errors/error", "Unable to complete request", "");
    }
    static void databaseError(HttpServletRequest req, HttpServletResponse res, SQLException exception)
            throws ServletException, IOException {
        req.getServletContext().log("Database failure; SQL state: " + exception.getSQLState());
        error(req, res, 503, "Clinic data is temporarily unavailable. Please try again shortly.");
    }
}
