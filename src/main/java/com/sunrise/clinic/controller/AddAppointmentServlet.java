package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.service.ValidationException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/appointments/new")
public class AddAppointmentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        showForm(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        AppointmentRequest request = new AppointmentRequest(req.getParameter("existingPatientId"),
                new PatientDraft(req.getParameter("fullName"), req.getParameter("address"), req.getParameter("phone")),
                req.getParameter("dentistId"), req.getParameter("treatmentId"),
                req.getParameter("date"), req.getParameter("time"));
        User user = (User) req.getSession().getAttribute("loggedUser");
        try {
            Appointment saved = Services.get(getServletContext()).appointments().register(request, user.id());
            req.getSession().setAttribute("success", "Appointment " + saved.appointmentNumber() + " registered successfully.");
            // Post/Redirect/Get avoids a new submission when the success page is refreshed.
            res.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (ValidationException exception) {
            res.setStatus(400);
            req.setAttribute("error", exception.getMessage());
            showForm(req, res);
        } catch (SQLException exception) {
            getServletContext().log("Appointment database failure; SQL state: " + exception.getSQLState());
            res.setStatus(503);
            req.setAttribute("error", "The appointment could not be confirmed. Please contact clinic staff before retrying.");
            showForm(req, res);
        }
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("pageTitle", "New appointment");
        req.setAttribute("activePage", "new");
        Services services = Services.get(getServletContext());
        try {
            req.setAttribute("patients", services.patients().list());
            req.setAttribute("dentists", services.dentists().listActive());
            req.setAttribute("treatments", services.treatments().listActive());
        } catch (SQLException exception) {
            getServletContext().log("Reference data failure; SQL state: " + exception.getSQLState());
            res.setStatus(503);
            req.setAttribute("referenceUnavailable", true);
            req.setAttribute("error", "Clinic reference data is temporarily unavailable. Please try again shortly.");
        }
        req.getRequestDispatcher("/WEB-INF/views/appointments/add-appointment.jsp").forward(req, res);
    }
}
