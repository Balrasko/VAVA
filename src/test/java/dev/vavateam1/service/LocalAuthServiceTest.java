package dev.vavateam1.service;

import dev.vavateam1.dao.UserDao;
import dev.vavateam1.dao.UserSessionDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalAuthServiceTest {

    @Mock UserDao userDao;
    @Mock UserSessionDao userSessionDao;

    @InjectMocks
    LocalAuthService authService;

    @Test
    void login_userNotFound_returnsFalse() {
        when(userDao.findByEmail("neexistuje@test.com")).thenReturn(Optional.empty());

        boolean result = authService.login("neexistuje@test.com", "heslo");

        assertFalse(result);
    }

    @Test
    void getUser_beforeLogin_returnsNull() {
        assertNull(authService.getUser());
    }

    @Test
    void logout_withoutLogin_doesNotThrow() {
        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    void logout_clearsCurrentUser() {
        authService.logout();
        assertNull(authService.getUser());
    }
}