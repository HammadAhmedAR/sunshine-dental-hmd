package com.sunrise.clinic.filter;

import com.sunrise.clinic.model.User;
import com.sunrise.clinic.util.Csrf;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "same-origin");
        res.setHeader("Content-Security-Policy", "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; form-action 'self'; frame-ancestors 'none'; base-uri 'self'");
        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        boolean publicPath = path.equals("/") || path.isEmpty() || path.equals("/index.jsp")
                || path.equals("/login") || path.equals("/api/health") || path.startsWith("/assets/");
        HttpSession session = req.getSession(false);
        if (!path.startsWith("/assets/")) res.setHeader("Cache-Control", "no-store");
        if (!publicPath && (session == null || !(session.getAttribute("loggedUser") instanceof User))) {
            if (path.startsWith("/api/")) {
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"error\":\"Authentication required.\"}");
            } else res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (path.equals("/login") || !publicPath) {
            Csrf.token(req.getSession());
        }
        if ("POST".equals(req.getMethod())) {
            String supplied = req.getParameter("csrfToken");
            String expected = session == null ? null : (String) session.getAttribute("csrfToken");
            if (supplied == null || expected == null || !MessageDigest.isEqual(
                    supplied.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) {
                res.sendError(403, "Your form expired. Reload the page and try again.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
