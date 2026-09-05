package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.service.ValidationException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/appointments")
public class AppointmentSearchServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            req.setAttribute("result", Services.get(getServletContext()).appointmentQueries()
                    .list(req.getParameter("date"), req.getParameter("status"), req.getParameter("page")));
            ViewSupport.page(req, res, "appointments/list", "Appointments", "appointments");
        } catch (ValidationException e) { ViewSupport.error(req, res, 400, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
}
