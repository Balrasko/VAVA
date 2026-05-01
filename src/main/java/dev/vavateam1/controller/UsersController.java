package dev.vavateam1.controller;

import java.util.List;

import com.google.inject.Inject;
import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;
import dev.vavateam1.service.AuthService;
import dev.vavateam1.service.UsersService;
import dev.vavateam1.util.I18n;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UsersController {

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @FXML private VBox usersList;
    @FXML private ScrollPane userFormPanel;
    @FXML private Label formTitle;
    @FXML private Label formErrorLabel;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private ToggleGroup roleToggleGroup;
    @FXML private ToggleButton waiterRoleButton;
    @FXML private ToggleButton chefRoleButton;
    @FXML private ToggleButton managerRoleButton;
    @FXML private Button deleteUserButton;
    @FXML private Button submitUserButton;

    private final UsersService usersService;
    private final AuthService authService;
    private List<UserWithSessionDto> users;
    private Integer editingUserId;

    @Inject
    public UsersController(UsersService usersService, AuthService authService) {
        this.usersService = usersService;
        this.authService = authService;
    }

    @FXML
    private void initialize() {
        configureRoleToggleGroup();
        reloadUsers();
        hideForm();
    }

    @FXML
    private void onCreateUser() {
        editingUserId = null;
        clearForm();
        formTitle.setText(I18n.t("users.createUser"));
        submitUserButton.setText(I18n.t("users.createUser"));
        deleteUserButton.setVisible(false);
        deleteUserButton.setManaged(false);
        showForm();
    }

    @FXML
    private void onCloseForm() {
        editingUserId = null;
        hideForm();
    }

    @FXML
    private void onSubmitUser() {
        String email = emailField.getText().trim();
        if (!email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            showFormError(I18n.t("login.invalidEmail"));
            return;
        }

        if (editingUserId == null && !passwordField.getText().isBlank() && !passwordField.getText().equals(repeatPasswordField.getText())) {
            showFormError(I18n.t("users.passwordsDoNotMatch"));
            return;
        }

        clearFormError();

        User user = new User();
        user.setName(buildFullName());
        user.setEmail(email.isBlank() ? "new.user@vava.com" : email);
        user.setRoleId(resolveSelectedRoleId());
        if (!passwordField.getText().isBlank()) {
            user.setPasswordHash(passwordField.getText());
        }

        if (editingUserId != null) {
            user.setId(editingUserId);
            usersService.updateUser(user);
        } else {
            usersService.createUser(user);
        }

        reloadUsers();
        editingUserId = null;
        hideForm();
    }

    @FXML
    private void onDeleteUser() {
        if (editingUserId == null) {
            return;
        }

        User currentUser = authService.getUser();
        if (currentUser != null && currentUser.getId() == editingUserId) {
            return;
        }

        User userToDelete = users.stream()
                .map(UserWithSessionDto::getUser)
                .filter(user -> user.getId() == editingUserId)
                .findFirst()
                .orElse(null);

        if (userToDelete == null) {
            return;
        }

        usersService.deleteUser(userToDelete);
        editingUserId = null;
        hideForm();
        reloadUsers();
    }

    private void showFormError(String message) {
        formErrorLabel.setText(message);
        formErrorLabel.setVisible(true);
        formErrorLabel.setManaged(true);
    }

    private void clearFormError() {
        formErrorLabel.setText("");
        formErrorLabel.setVisible(false);
        formErrorLabel.setManaged(false);
    }

    private void reloadUsers() {
        users = usersService.getAllUsers();
        renderUsers();
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
        selectRole(waiterRoleButton);
        clearFormError();
    }

    private void configureRoleToggleGroup() {
        if (roleToggleGroup == null) {
            roleToggleGroup = new ToggleGroup();
        }

        waiterRoleButton.setToggleGroup(roleToggleGroup);
        chefRoleButton.setToggleGroup(roleToggleGroup);
        managerRoleButton.setToggleGroup(roleToggleGroup);

        roleToggleGroup.selectedToggleProperty().addListener((observable, previousToggle, selectedToggle) -> {
            if (selectedToggle == null) {
                selectRole(previousToggle != null ? previousToggle : waiterRoleButton);
            }
        });

        if (roleToggleGroup.getSelectedToggle() == null) {
            selectRole(waiterRoleButton);
        }
    }

    private void selectRole(Toggle toggle) {
        if (toggle != null) {
            roleToggleGroup.selectToggle(toggle);
        }
    }

    private String buildFullName() {
        String firstName = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        if (firstName.isBlank() && surname.isBlank()) return I18n.t("users.newUser");
        if (surname.isBlank()) return firstName;
        if (firstName.isBlank()) return surname;
        return firstName + " " + surname;
    }

    private int resolveSelectedRoleId() {
        Toggle selectedRole = roleToggleGroup.getSelectedToggle();
        if (selectedRole == managerRoleButton) return 1;
        if (selectedRole == chefRoleButton) return 3;
        return 2;
    }

    private String roleIdToName(int roleId) {
        return switch (roleId) {
            case 1 -> I18n.t("role.manager");
            case 3 -> I18n.t("role.chef");
            default -> I18n.t("role.waiter");
        };
    }

    private boolean isActive(UserWithSessionDto dto) {
        return dto.getSession() != null && dto.getSession().getLogoutTime() == null;
    }

    private VBox createUserCard(UserWithSessionDto dto) {
        User user = dto.getUser();
        boolean active = isActive(dto);

        VBox card = new VBox(14);
        card.getStyleClass().add("user-card");

        Label nameLabel = new Label(user.getName());
        nameLabel.getStyleClass().add("user-name");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("user-email");

        VBox identityBox = new VBox(8, nameLabel, emailLabel);
        HBox.setHgrow(identityBox, Priority.ALWAYS);

        Label roleLabel = new Label(roleIdToName(user.getRoleId()));
        roleLabel.getStyleClass().add("user-meta");

        Label statusText = new Label(active ? I18n.t("status.online") : I18n.t("status.offline"));
        statusText.getStyleClass().add("user-meta");

        Region statusDot = new Region();
        statusDot.getStyleClass().add("status-dot");
        statusDot.getStyleClass().add(active ? "status-dot-online" : "status-dot-offline");

        HBox detailsRow = new HBox(18, roleLabel, statusText, statusDot);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button(I18n.t("common.edit"));
        editButton.getStyleClass().add("secondary-action-button");
        editButton.setOnAction(event -> startEditingUser(dto));

        HBox actionsRow = new HBox(12, spacer, editButton);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        HBox bottomRow = new HBox(18, detailsRow, actionsRow);
        HBox.setHgrow(detailsRow, Priority.ALWAYS);
        HBox.setHgrow(actionsRow, Priority.ALWAYS);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(identityBox, bottomRow);
        return card;
    }

    private void startEditingUser(UserWithSessionDto dto) {
        editingUserId = dto.getUser().getId();
        populateForm(dto);
        formTitle.setText(I18n.t("users.editUser"));
        submitUserButton.setText(I18n.t("users.saveUser"));
        deleteUserButton.setVisible(true);
        deleteUserButton.setManaged(true);

        User currentUser = authService.getUser();
        boolean editingCurrentUser = currentUser != null && currentUser.getId() == editingUserId;
        deleteUserButton.setDisable(editingCurrentUser);
        showForm();
    }

    private void populateForm(UserWithSessionDto dto) {
        User user = dto.getUser();
        String[] parts = user.getName().split("\\s+", 2);
        nameField.setText(parts.length > 0 ? parts[0] : "");
        surnameField.setText(parts.length > 1 ? parts[1] : "");
        emailField.setText(user.getEmail());
        passwordField.clear();
        repeatPasswordField.clear();
        switch (user.getRoleId()) {
            case 1 -> selectRole(managerRoleButton);
            case 3 -> selectRole(chefRoleButton);
            default -> selectRole(waiterRoleButton);
        }
    }
}
