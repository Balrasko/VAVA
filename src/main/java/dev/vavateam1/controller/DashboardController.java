package dev.vavateam1.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;

import dev.vavateam1.model.Table;
import dev.vavateam1.model.User;
import dev.vavateam1.service.AuthService;
// animácie
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {
    private final AuthService authService;
    private final ViewSwitcher viewSwitcher;
    private final Injector injector;

    @Inject
    public DashboardController(AuthService authService, ViewSwitcher viewSwitcher, Injector injector) {
        this.authService = authService;
        this.viewSwitcher = viewSwitcher;
        this.injector = injector;
    }

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebar;

    @FXML
    private VBox topSection;

    @FXML
    private Label loggedInRoleLabel;

    @FXML
    private TopNavbarController topNavbarController;

    private boolean sidebarVisible = false;

    @FXML
    public void initialize() {
        User currentUser = authService.getUser();
        boolean isChef = currentUser != null && currentUser.getRoleId() == 3;

        sidebar.setPrefWidth(0);
        sidebar.setMinWidth(0);
        sidebar.setMaxWidth(0);
        sidebarVisible = false;

        if (loggedInRoleLabel != null && currentUser != null) {
            loggedInRoleLabel.setText(getRoleName(currentUser.getRoleId()));
        }

        if (topNavbarController != null) {
            topNavbarController.setOnTabSelected(this::handleTopTabSelected);
        }

        try {
            if (isChef) {
                showChefView();
                sidebar.setVisible(false);
                sidebar.setManaged(false);
                topSection.setVisible(false);
                topSection.setManaged(false);
            } else {
                showTableView();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void toggleSidebar() {
        User currentUser = authService.getUser();
        if (currentUser != null && currentUser.getRoleId() == 3) {
            return;
        }

        double endWidth = sidebarVisible ? 0 : 220;

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(250),
                new KeyValue(sidebar.prefWidthProperty(), endWidth),
                new KeyValue(sidebar.minWidthProperty(), endWidth),
                new KeyValue(sidebar.maxWidthProperty(), endWidth)));

        timeline.play();
        sidebarVisible = !sidebarVisible;
    }

    private void setContent(String text) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label(text));
    }

    private void handleTopTabSelected(String tabName) {
        try {
            switch (tabName) {
                case "tableLayout" -> showTableView();
                case "finances" -> showFinances();
                case "users" -> setContent("Users placeholder");
                case "menu" -> setContent("Menu placeholder");
                case "inventory" -> setContent("Inventory placeholder");
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTableView() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/tables.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent view = loader.load();

        TablesController controller = loader.getController();
        controller.setDashboardController(this);

        contentArea.getChildren().setAll(view);

        if (topNavbarController != null) {
            topNavbarController.setActiveTab("tableLayout");
        }
    }

    @FXML
    public void showOrderView(Table table) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/tempOrder.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent view = loader.load();

        TempOrderController controller = loader.getController();
        controller.setTable(table);

        contentArea.getChildren().setAll(view);
    }

    private void showChefView() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/tempOrder.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent view = loader.load();
        contentArea.getChildren().setAll(view);
    }

    @FXML
    private void showClosing() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/closing.fxml")
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHistory() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/history.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void showManager() {
        
    }
    
    @FXML
    private void showFinances() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/finances.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() throws Exception {
        authService.logout();
        viewSwitcher.SetView("/view/login.fxml");
    }

    private String getRoleName(int roleId) {
        return switch (roleId) {
            case 1 -> "Admin";
            case 2 -> "Waiter";
            case 3 -> "Chef";
            default -> "User";
        };
    }
}
