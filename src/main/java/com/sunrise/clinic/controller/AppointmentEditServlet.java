package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.model.AppointmentDetails;
import com.sunrise.clinic.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/appointments/edit")
public class AppointmentEditServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        show(req, res);
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Services.get(getServletContext()).appointmentManagement().reschedule(req.getParameter("number"),
                    req.getParameter("dentistId"), req.getParameter("treatmentId"), req.getParameter("date"), req.getParameter("time"));
            req.getSession().setAttribute("success", "Appointment rescheduled. Its appointment number is unchanged.");
            res.sendRedirect(req.getContextPath() + "/appointments/details?number="
                    + URLEncoder.encode(req.getParameter("number").trim(), StandardCharsets.UTF_8));
        } catch (ValidationException e) {
            res.setStatus(400); req.setAttribute("error", e.getMessage()); show(req, res);
        } catch (RecordNotFoundException e) { ViewSupport.error(req, res, 404, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
    private void show(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Services services = Services.get(getServletContext());
            AppointmentDetails appointment = services.appointmentQueries().find(req.getParameter("number"));
            if (!appointment.isEditable()) {
                ViewSupport.error(req, res, 409, "This appointment cannot be rescheduled."); return;
            }
            req.setAttribute("appointment", appointment);
            req.setAttribute("dentists", services.dentists().listActive());
            req.setAttribute("treatments", services.treatments().listActive());
            boolean posted = "POST".equals(req.getMethod());
            req.setAttribute("formDentist", posted ? req.getParameter("dentistId") : appointment.getDentistIdValue());
            req.setAttribute("formTreatment", posted ? req.getParameter("treatmentId") : appointment.getTreatmentIdValue());
            req.setAttribute("formDate", posted ? req.getParameter("date") : appointment.getDate());
            req.setAttribute("formTime", posted ? req.getParameter("time") : appointment.getTime());
            ViewSupport.page(req, res, "appointments/edit", "Reschedule appointment", "appointments");
        } catch (ValidationException e) { ViewSupport.error(req, res, 400, e.getMessage()); }
        catch (RecordNotFoundException e) { ViewSupport.error(req, res, 404, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
}
