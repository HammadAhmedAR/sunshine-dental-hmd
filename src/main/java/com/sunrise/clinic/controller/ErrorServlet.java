package com.sunrise.clinic.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/error")
public class ErrorServlet extends HttpServlet {
    @Override protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Object status = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = status instanceof Integer ? (Integer) status : 500;
        Throwable failure = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (failure != null) getServletContext().log("Unhandled request failure: " + failure.getClass().getName());
        ViewSupport.error(req, res, code, switch (code) {
            case 403 -> "Your form expired or access was denied. Reload the page and try again.";
            case 404 -> "The requested page or record could not be found.";
            case 405 -> "This action requires the appropriate form. Return to the workspace.";
            default -> "The request could not be completed. Return to the workspace and try again.";
        });
    }
}
