package dev.vavateam1.controller;

import java.io.IOException;

import com.google.inject.Inject;

import dev.vavateam1.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private final AuthService authService;
    private final ViewSwitcher viewSwitcher;

    @Inject
    public LoginController(AuthService authService, ViewSwitcher viewSwitcher) {
        this.authService = authService;
        this.viewSwitcher = viewSwitcher;
    }

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() throws IOException {

        String email = emailField.getText();
        String password = passwordField.getText();

        if (authService.login(email, password)) {
            viewSwitcher.SetView("/view/dashboard.fxml");
        } else {
            errorLabel.setText("Invalid credentials");
        }
    }
}
