package dev.vavateam1.controller;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UsersController {

    @FXML
    private VBox usersList;

    @FXML
    private VBox userFormPanel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private CheckBox managerCheckBox;

    @FXML
    private CheckBox waiterCheckBox;

    @FXML
    private CheckBox chefCheckBox;

    @FXML
    private Button submitUserButton;

    private final List<UserRow> users = new ArrayList<>();
    private Integer editingUserIndex;

    @FXML
    private void initialize() {
        users.addAll(List.of(
                new UserRow("Ferko Mrkvicka", "ferko.mrkvicka@gmail.com", "Waiter", true),
                new UserRow("Ferko Hrasko", "janko.hrasko@gmail.com", "Chef", false)
        ));
        hideForm();
        renderUsers();
    }

    @FXML
    private void onCreateUser() {
        editingUserIndex = null;
        clearForm();
        if (submitUserButton != null) {
            submitUserButton.setText("Create user");
        }
        showForm();
    }

    @FXML
    private void onCloseForm() {
        editingUserIndex = null;
        hideForm();
    }

    @FXML
    private void onSubmitUser() {
        String fullName = buildFullName();
        String email = emailField.getText().isBlank() ? "new.user@vava.com" : emailField.getText().trim();
        String role = resolveSelectedRole();
        UserRow updatedRow = new UserRow(fullName, email, role, false);

        if (editingUserIndex != null) {
            users.set(editingUserIndex, updatedRow);
        } else {
            users.add(updatedRow);
        }

        renderUsers();
        editingUserIndex = null;
        hideForm();
    }

    private void renderUsers() {
        usersList.getChildren().clear();
        users.stream()
                .map(this::createUserCard)
                .forEach(usersList.getChildren()::add);
    }

    private void showForm() {
        userFormPanel.setVisible(true);
        userFormPanel.setManaged(true);
    }

    private void hideForm() {
        userFormPanel.setVisible(false);
        userFormPanel.setManaged(false);
    }

    private void clearForm() {
        nameField.clear();
        surnameField.clear();
        emailField.clear();
        passwordField.clear();
        repeatPasswordField.clear();
        managerCheckBox.setSelected(false);
        waiterCheckBox.setSelected(true);
        chefCheckBox.setSelected(false);
    }

    private String buildFullName() {
        String firstName = nameField.getText().trim();
        String surname = surnameField.getText().trim();

        if (firstName.isBlank() && surname.isBlank()) {
            return "New User";
        }
        if (surname.isBlank()) {
            return firstName;
        }
        if (firstName.isBlank()) {
            return surname;
        }
        return firstName + " " + surname;
    }

    private String resolveSelectedRole() {
        if (managerCheckBox.isSelected()) {
            return "Manager";
        }
        if (chefCheckBox.isSelected()) {
            return "Chef";
        }
        return "Waiter";
    }

    private VBox createUserCard(UserRow row) {
        VBox card = new VBox(14);
        card.getStyleClass().add("user-card");

        Label nameLabel = new Label(row.name());
        nameLabel.getStyleClass().add("user-name");

        Label emailLabel = new Label(row.email());
        emailLabel.getStyleClass().add("user-email");

        VBox identityBox = new VBox(8, nameLabel, emailLabel);
        HBox.setHgrow(identityBox, Priority.ALWAYS);

        Label roleLabel = new Label(row.role());
        roleLabel.getStyleClass().add("user-meta");

        Label statusText = new Label("Logged");
        statusText.getStyleClass().add("user-meta");

        Region statusDot = new Region();
        statusDot.getStyleClass().add("status-dot");
        statusDot.getStyleClass().add(row.logged() ? "status-dot-online" : "status-dot-offline");

        HBox detailsRow = new HBox(18, roleLabel, statusText, statusDot);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("table-action-button");
        editButton.setOnAction(event -> startEditingUser(row));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("table-action-button");
        deleteButton.setOnAction(event -> deleteUser(row));

        HBox actionsRow = new HBox(14, spacer, editButton, deleteButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        HBox bottomRow = new HBox(18, detailsRow, actionsRow);
        HBox.setHgrow(detailsRow, Priority.ALWAYS);
        HBox.setHgrow(actionsRow, Priority.ALWAYS);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(identityBox, bottomRow);
        return card;
    }

    private void startEditingUser(UserRow row) {
        editingUserIndex = users.indexOf(row);
        populateForm(row);
        if (submitUserButton != null) {
            submitUserButton.setText("Save user");
        }
        showForm();
    }

    private void deleteUser(UserRow row) {
        users.remove(row);
        if (editingUserIndex != null && editingUserIndex >= users.size()) {
            editingUserIndex = null;
        }
        renderUsers();
    }

    private void populateForm(UserRow row) {
        String[] parts = row.name().split("\\s+", 2);
        nameField.setText(parts.length > 0 ? parts[0] : "");
        surnameField.setText(parts.length > 1 ? parts[1] : "");
        emailField.setText(row.email());
        passwordField.clear();
        repeatPasswordField.clear();
        managerCheckBox.setSelected("Manager".equalsIgnoreCase(row.role()));
        waiterCheckBox.setSelected("Waiter".equalsIgnoreCase(row.role()));
        chefCheckBox.setSelected("Chef".equalsIgnoreCase(row.role()));
    }

    private record UserRow(String name, String email, String role, boolean logged) {
    }
}
