package dev.vavateam1.controller;

import java.util.List;

import com.google.inject.Inject;
import dev.vavateam1.dto.UserWithSessionDto;
import dev.vavateam1.model.User;
import dev.vavateam1.service.AuthService;
import dev.vavateam1.service.UsersService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class UsersController {

    @FXML private VBox usersList;
    @FXML private ScrollPane userFormPanel;
    @FXML private Label formTitle;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private ToggleGroup roleToggleGroup;
    @FXML private RadioButton waiterRadio;
    @FXML private RadioButton chefRadio;
    @FXML private RadioButton managerRadio;
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
        reloadUsers();
        hideForm();
    }

    @FXML
    private void onCreateUser() {
        editingUserId = null;
        clearForm();
        formTitle.setText("Create user");
        submitUserButton.setText("Create user");
        showForm();
    }

    @FXML
    private void onCloseForm() {
        editingUserId = null;
        hideForm();
    }

    @FXML
    private void onSubmitUser() {
        User user = new User();
        user.setName(buildFullName());
        user.setEmail(emailField.getText().isBlank() ? "new.user@vava.com" : emailField.getText().trim());
        user.setRoleId(resolveSelectedRoleId());
        if (!passwordField.getText().isBlank()) {
            user.setPassword(passwordField.getText());
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
    }

    private String buildFullName() {
        String firstName = nameField.getText().trim();
        String surname = surnameField.getText().trim();
        if (firstName.isBlank() && surname.isBlank()) return "New User";
        if (surname.isBlank()) return firstName;
        if (firstName.isBlank()) return surname;
        return firstName + " " + surname;
    }

    private int resolveSelectedRoleId() {
        if (managerRadio.isSelected()) return 1;
        if (chefRadio.isSelected()) return 3;
        return 2;
    }

    private String roleIdToName(int roleId) {
        return switch (roleId) {
            case 1 -> "Manager";
            case 3 -> "Chef";
            default -> "Waiter";
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

        Label statusText = new Label(active ? "Online" : "Offline");
        statusText.getStyleClass().add("user-meta");

        Region statusDot = new Region();
        statusDot.getStyleClass().add("status-dot");
        statusDot.getStyleClass().add(active ? "status-dot-online" : "status-dot-offline");

        HBox detailsRow = new HBox(18, roleLabel, statusText, statusDot);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("table-action-button");
        editButton.setOnAction(event -> startEditingUser(dto));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("table-action-button");
        deleteButton.setOnAction(event -> deleteUser(dto));
        User currentUser = authService.getUser();
        if (currentUser != null && currentUser.getId() == user.getId()) {
            deleteButton.setDisable(true);
        }

        HBox actionsRow = new HBox(14, spacer, editButton, deleteButton);
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
        formTitle.setText("Edit user");
        submitUserButton.setText("Save user");
        showForm();
    }

    private void deleteUser(UserWithSessionDto dto) {
        usersService.deleteUser(dto.getUser());
        reloadUsers();
    }

    private void populateForm(UserWithSessionDto dto) {
        User user = dto.getUser();
        String[] parts = user.getName().split("\\s+", 2);
        nameField.setText(parts.length > 0 ? parts[0] : "");
        surnameField.setText(parts.length > 1 ? parts[1] : "");
        emailField.setText(user.getEmail());
        passwordField.clear();
        repeatPasswordField.clear();
        managerRadio.setSelected(user.getRoleId() == 1);
        chefRadio.setSelected(user.getRoleId() == 3);
        waiterRadio.setSelected(user.getRoleId() == 2);
    }
}
