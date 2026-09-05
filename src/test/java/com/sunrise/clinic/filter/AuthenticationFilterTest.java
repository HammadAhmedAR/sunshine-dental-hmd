package com.sunrise.clinic.filter;

import com.sunrise.clinic.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;

class AuthenticationFilterTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private AuthenticationFilter filter;

    @BeforeEach void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        filter = new AuthenticationFilter();
        when(request.getContextPath()).thenReturn("/clinic");
        when(request.getMethod()).thenReturn("GET");
    }
    @Test void redirectsAnonymousBusinessRequest() throws Exception {
        when(request.getServletPath()).thenReturn("/appointments/new");
        filter.doFilter(request, response, chain);
        verify(response).sendRedirect("/clinic/login");
        verifyNoInteractions(chain);
    }
    @Test void healthRemainsPublicWithJaxRsPathInfo() throws Exception {
        when(request.getServletPath()).thenReturn("/api");
        when(request.getPathInfo()).thenReturn("/health");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verify(request, never()).getSession();
    }
    @Test void assetsRemainPublic() throws Exception {
        when(request.getServletPath()).thenReturn("/assets/css/app.css");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
    @Test void rejectsForgedAuthenticatedPost() throws Exception {
        HttpSession session = signedInSession();
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute("csrfToken")).thenReturn("expected-token");
        when(request.getParameter("csrfToken")).thenReturn("forged-token");
        filter.doFilter(request, response, chain);
        verify(response).sendError(403, "Your form expired. Reload the page and try again.");
        verifyNoInteractions(chain);
    }
    @Test void acceptsAuthenticatedPostWithCsrfToken() throws Exception {
        HttpSession session = signedInSession();
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute("csrfToken")).thenReturn("expected-token");
        when(request.getParameter("csrfToken")).thenReturn("expected-token");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verify(response).setHeader("Cache-Control", "no-store");
    }
    @Test void missingLoginCsrfTokenRejected() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getServletPath()).thenReturn("/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);
        filter.doFilter(request, response, chain);
        verify(response).sendError(403, "Your form expired. Reload the page and try again.");
    }
    @Test void anonymousApiReturnsJson401WithoutRedirect() throws Exception {
        when(request.getServletPath()).thenReturn("/api");
        when(request.getPathInfo()).thenReturn("/appointments/APT-2026-00001");
        java.io.StringWriter body = new java.io.StringWriter();
        when(response.getWriter()).thenReturn(new java.io.PrintWriter(body));
        filter.doFilter(request, response, chain);
        verify(response).setStatus(401);
        verify(response, never()).sendRedirect(anyString());
        verify(response).setHeader("Cache-Control", "no-store");
        org.junit.jupiter.api.Assertions.assertTrue(body.toString().contains("Authentication required"));
        verifyNoInteractions(chain);
    }
    private HttpSession signedInSession() {
        HttpSession session = mock(HttpSession.class);
        when(request.getServletPath()).thenReturn("/appointments/new");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("loggedUser")).thenReturn(new User(1, "staff", "Clinic Staff", "STAFF"));
        return session;
    }
}
