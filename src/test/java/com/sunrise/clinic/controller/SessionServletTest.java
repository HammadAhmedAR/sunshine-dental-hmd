package com.sunrise.clinic.controller;

import com.sunrise.clinic.config.Services;
import com.sunrise.clinic.model.User;
import com.sunrise.clinic.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;

class SessionServletTest {
    @Test void successfulLoginInvalidatesOldSessionAndStoresSafeUser() throws Exception {
        ServletContext context = mock(ServletContext.class);
        ServletConfig config = mock(ServletConfig.class);
        Services services = mock(Services.class);
        AuthService auth = mock(AuthService.class);
        when(config.getServletContext()).thenReturn(context);
        when(context.getAttribute(Services.class.getName())).thenReturn(services);
        when(services.auth()).thenReturn(auth);
        User user = new User(1, "staff", "Clinic Staff", "STAFF");
        when(auth.authenticate("staff", "password")).thenReturn(Optional.of(user));
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession oldSession = mock(HttpSession.class);
        HttpSession newSession = mock(HttpSession.class);
        when(request.getParameter("username")).thenReturn("staff");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getContextPath()).thenReturn("/clinic");
        when(request.getSession()).thenReturn(oldSession);
        when(request.getSession(true)).thenReturn(newSession);
        LoginServlet servlet = new LoginServlet();
        servlet.init(config);
        servlet.doPost(request, response);
        var order = inOrder(oldSession, newSession, response);
        order.verify(oldSession).invalidate();
        order.verify(newSession).setAttribute("loggedUser", user);
        order.verify(response).sendRedirect("/clinic/dashboard");
    }
    @Test void logoutInvalidatesSessionAndReturnsToLogin() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/clinic");
        new LogoutServlet().doPost(request, response);
        verify(session).invalidate();
        verify(response).sendRedirect("/clinic/login");
    }
}
