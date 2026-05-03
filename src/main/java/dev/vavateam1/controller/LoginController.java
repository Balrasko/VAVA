package dev.vavateam1.controller;

import java.io.IOException;

import com.google.inject.Inject;

import dev.vavateam1.service.AuthService;
import dev.vavateam1.util.I18n;
import dev.vavateam1.util.ValidationUtils;
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

    @FXML
    private Button languageButton;

    @FXML
    private void initialize() {
        if (languageButton != null) {
            languageButton.setText(I18n.nextLanguageCode());
        }
    }

    @FXML
    private void switchLanguage() throws IOException {
        I18n.toggleLocale();
        viewSwitcher.reloadCurrentView();
    }

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!ValidationUtils.isValidEmail(email)) {
            errorLabel.setText(I18n.t("login.invalidEmail"));
            return;
        }

        loginButton.setDisable(true);

        if (authService.login(email, password)) {
            viewSwitcher.SetView("/view/dashboard.fxml");
        } else {
            errorLabel.setText(I18n.t("login.invalidCredentials"));
            loginButton.setDisable(false);
        }
    }
}
