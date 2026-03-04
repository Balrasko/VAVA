package dev.vavateam1.service;

public class MockAuthService implements AuthService {

    @Override
    public boolean login(String email, String password) {
        return "admin".equals(email) && "admin".equals(password);
    }
    public void logout() {
        // zatiaľ nič nerobí (mock)
    }
}

