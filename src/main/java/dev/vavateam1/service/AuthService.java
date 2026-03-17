package dev.vavateam1.service;

import dev.vavateam1.model.User;

public interface AuthService {
    public boolean login(String emailOrUsername, String password);

    public User getUser();

    public void logout();
}
