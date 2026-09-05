package com.sunrise.clinic.service;

import com.sunrise.clinic.dao.UserDAO;
import com.sunrise.clinic.model.User;
import com.sunrise.clinic.util.PasswordHasher;
import org.junit.jupiter.api.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private static final PasswordHasher HASHER = new PasswordHasher();
    private static final String HASH = HASHER.hash("SunriseLocal!2026");
    private UserDAO users;
    private AuthService service;
    private final User user = new User(1, "sunrise.admin", "Sunrise Administrator", "ADMIN");

    @BeforeEach void setUp() {
        users = mock(UserDAO.class);
        service = new AuthService(users, HASHER);
    }
    @Test void validCredentialsReturnSafeIdentity() throws Exception {
        when(users.findByUsername("sunrise.admin")).thenReturn(Optional.of(new UserDAO.Credentials(user, HASH, true)));
        assertEquals(user, service.authenticate("sunrise.admin", "SunriseLocal!2026").orElseThrow());
    }
    @Test void wrongPasswordRejected() throws Exception {
        when(users.findByUsername("sunrise.admin")).thenReturn(Optional.of(new UserDAO.Credentials(user, HASH, true)));
        assertTrue(service.authenticate("sunrise.admin", "wrong").isEmpty());
    }
    @Test void unknownUsernameRejected() throws Exception {
        when(users.findByUsername("missing")).thenReturn(Optional.empty());
        assertTrue(service.authenticate("missing", "wrong").isEmpty());
    }
    @Test void inactiveAccountRejected() throws Exception {
        when(users.findByUsername("sunrise.admin")).thenReturn(Optional.of(new UserDAO.Credentials(user, HASH, false)));
        assertTrue(service.authenticate("sunrise.admin", "SunriseLocal!2026").isEmpty());
    }
    @Test void overlongUtf8PasswordRejectedBeforeDao() throws Exception {
        assertTrue(service.authenticate("sunrise.admin", "é".repeat(37)).isEmpty());
        verifyNoInteractions(users);
    }
    @Test void malformedHashCannotAuthenticate() {
        assertFalse(HASHER.verify("test", "plaintext"));
    }
}
