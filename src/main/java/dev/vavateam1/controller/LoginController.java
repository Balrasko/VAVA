package dev.vavateam1.controller;

import java.io.IOException;

import com.google.inject.Inject;

import dev.vavateam1.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private Button loginButton;

    // https://emailregex.com/index.html
    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern
            .compile(
                    "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorLabel.setText("Please enter a valid email address");
            return;
        }

        loginButton.setDisable(true);

        if (authService.login(email, password)) {
            viewSwitcher.SetView("/view/dashboard.fxml");
        } else {
            errorLabel.setText("Invalid credentials");
            loginButton.setDisable(false);
        }
    }
}
