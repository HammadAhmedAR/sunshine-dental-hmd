package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet("/reports")
public class ReportsServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String today = LocalDate.now(DashboardService.CLINIC_ZONE).toString();
        String date = req.getParameter("date") == null ? today : req.getParameter("date");
        String from = req.getParameter("from") == null ? today : req.getParameter("from");
        String to = req.getParameter("to") == null ? today : req.getParameter("to");
        req.setAttribute("reportDate", date); req.setAttribute("reportFrom", from); req.setAttribute("reportTo", to);
        try {
            ReportService service = Services.get(getServletContext()).reports();
            req.setAttribute("schedule", service.schedule(date));
            req.setAttribute("revenue", service.revenue(from, to));
            ViewSupport.page(req, res, "reports/reports", "Clinic reports", "reports");
        } catch (ValidationException e) { ViewSupport.error(req, res, 400, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
}
