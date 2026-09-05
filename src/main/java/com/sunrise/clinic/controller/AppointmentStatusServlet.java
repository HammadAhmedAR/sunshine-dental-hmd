package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/appointments/status")
public class AppointmentStatusServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Services.get(getServletContext()).appointmentManagement().changeStatus(req.getParameter("number"), req.getParameter("status"));
            req.getSession().setAttribute("success", "Appointment status updated.");
            res.sendRedirect(req.getContextPath() + "/appointments/details?number="
                    + URLEncoder.encode(req.getParameter("number").trim(), StandardCharsets.UTF_8));
        } catch (ValidationException e) { ViewSupport.error(req, res, 400, e.getMessage()); }
        catch (RecordNotFoundException e) { ViewSupport.error(req, res, 404, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
}
