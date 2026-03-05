package dev.vavateam1.controller;

import dev.vavateam1.service.AuthService;
import dev.vavateam1.service.BasicAuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {
    // TODO: inject
    private final AuthService authService = new BasicAuthService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;


    @FXML
    private void handleLogin() {

        String email = emailField.getText();
        String password = passwordField.getText();

        if (authService.login(email, password)) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dashboard.fxml"));

                Scene scene = new Scene(loader.load(), 1200, 800);

                // ✅ TU JE FIX
                scene.getStylesheets()
                        .add(getClass().getResource("/css/style.css").toExternalForm());

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(scene);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            errorLabel.setText("Invalid credentials");
        }
    }
}
