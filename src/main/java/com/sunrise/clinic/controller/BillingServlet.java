package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.model.*;
import com.sunrise.clinic.service.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns={"/billing", "/billing/generate", "/billing/receipt"})
public class BillingServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            BillService service = Services.get(getServletContext()).bills();
            switch (req.getServletPath()) {
                case "/billing/generate" -> {
                    req.setAttribute("preview", service.preview(req.getParameter("number")));
                    ViewSupport.page(req, res, "billing/generate", "Generate bill", "billing");
                }
                case "/billing/receipt" -> {
                    req.setAttribute("bill", service.receipt(req.getParameter("number"), req.getParameter("appointment")));
                    ViewSupport.page(req, res, "billing/receipt", "Bill / receipt", "billing");
                }
                default -> {
                    req.setAttribute("result", service.history(req.getParameter("page")));
                    ViewSupport.page(req, res, "billing/history", "Billing history", "billing");
                }
            }
        } catch (ValidationException e) { ViewSupport.error(req, res, 400, e.getMessage()); }
        catch (RecordNotFoundException e) { ViewSupport.error(req, res, 404, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!"/billing/generate".equals(req.getServletPath())) { res.sendError(405); return; }
        try {
            User user = (User) req.getSession().getAttribute("loggedUser");
            Bill saved = Services.get(getServletContext()).bills().create(req.getParameter("number"), user.id());
            req.getSession().setAttribute("success", "Bill generated successfully.");
            res.sendRedirect(req.getContextPath() + "/billing/receipt?number=" + URLEncoder.encode(saved.billNumber(), StandardCharsets.UTF_8));
        } catch (ValidationException e) { ViewSupport.error(req, res, 400, e.getMessage()); }
        catch (RecordNotFoundException e) { ViewSupport.error(req, res, 404, e.getMessage()); }
        catch (SQLException e) { ViewSupport.databaseError(req, res, e); }
    }
}
