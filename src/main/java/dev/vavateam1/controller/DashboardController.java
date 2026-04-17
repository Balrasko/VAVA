package dev.vavateam1.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;

import dev.vavateam1.model.Table;
import dev.vavateam1.model.User;
import dev.vavateam1.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {
    private static final double SIDEBAR_OPEN_WIDTH = 208;

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
    private HBox managerPanelItem;

    @FXML
    private HBox topNavbarContainer;

    private TopNavbarController topNavbarController;
    private TopNavbarZonesController topNavbarZonesController;
    private TablesController tablesController;
    private int activeZoneId = 1;

    private boolean sidebarVisible = false;

    private boolean isAdmin = false;

    @FXML
    public void initialize() {
        User currentUser = authService.getUser();
        boolean isKitchenStaff = currentUser != null && currentUser.getRoleId() == 3;
        isAdmin = currentUser != null && currentUser.getRoleId() == 1;

        sidebar.setPrefWidth(0);
        sidebar.setMinWidth(0);
        sidebar.setMaxWidth(0);
        sidebarVisible = false;

        if (loggedInRoleLabel != null && currentUser != null) {
            loggedInRoleLabel.setText(getRoleName(currentUser.getRoleId()));
        }

        if (managerPanelItem != null) {
            managerPanelItem.setVisible(isAdmin);
            managerPanelItem.setManaged(isAdmin);
        }

        setTopNavbarVisible(false);

        try {
            if (isKitchenStaff) {
                showKitchenView();
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

        double endWidth = sidebarVisible ? 0 : SIDEBAR_OPEN_WIDTH;

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

    private void setTopNavbarVisible(boolean visible) {
        if (topNavbarContainer != null) {
            topNavbarContainer.setVisible(visible);
            topNavbarContainer.setManaged(visible);
            if (!visible) {
                topNavbarContainer.getChildren().clear();
                topNavbarController = null;
            }
        }
        if (!visible) {
            topNavbarZonesController = null;
        }
    }

    private void loadManagerTopNavbar() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/top-navbar.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent navbar = loader.load();
        topNavbarController = loader.getController();

        if (topNavbarController != null) {
            topNavbarController.setOnTabSelected(this::handleTopTabSelected);
            topNavbarController.setActiveTab("tableLayout");
        }

        topNavbarContainer.getChildren().setAll(navbar);
        setTopNavbarVisible(true);
    }

    /** Loads zones bar into top row for normal waiter table view. */
    private void loadZonesTopNavbar() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/top-navbar-zones.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent navbar = loader.load();
        topNavbarZonesController = loader.getController();
        topNavbarController = null;

        if (topNavbarZonesController != null) {
            topNavbarZonesController.setAddZoneTabVisible(false);
            topNavbarZonesController.setOnZoneSelected(this::handleZoneSelected);
            topNavbarZonesController.setActiveZone(activeZoneId);
        }

        topNavbarContainer.getChildren().setAll(navbar);
        setTopNavbarVisible(true);
    }

    private void handleZoneSelected(int zoneId) {
        activeZoneId = zoneId;
        if (tablesController != null) {
            tablesController.setActiveZone(zoneId);
        }
    }

    private void handleTopTabSelected(String tabName) {
        try {
            switch (tabName) {
                case "tableLayout" -> showTableLayout();
                case "finances" -> showFinances();
                case "inventory" -> showInventory();
                case "users" -> showUsers();
                case "menu" -> showMenu();
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showTableView() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/tables.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent view = loader.load();

        tablesController = loader.getController();
        tablesController.setDashboardController(this);
        tablesController.setActiveZone(activeZoneId);

        contentArea.getChildren().setAll(view);

        loadZonesTopNavbar();
    }

    @FXML
    public void showOrderView(Table table) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/order.fxml"));
        loader.setControllerFactory(injector::getInstance);

        Parent view = loader.load();

        OrderController controller = loader.getController();
        controller.initData(table, this);

        contentArea.getChildren().setAll(view);
        setTopNavbarVisible(false);
    }

    private void showKitchenView() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/kitchen.xml"));
        loader.setControllerFactory(injector::getInstance);

        Parent view = loader.load();
        contentArea.getChildren().setAll(view);
    }

    @FXML
    private void showClosing() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/closing.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            setTopNavbarVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/managermenu.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUsers() {
        if (!isAdmin) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/users.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;

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
            tablesController = null;
            setTopNavbarVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void showManager() {
        if (!isAdmin) {
            return;
        }

        tablesController = null;
        try {
            loadManagerTopNavbar();
            showTableLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showFinances() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/finances.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showInventory() {
        if (!isAdmin) {
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/inventory.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTableLayout() {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/tableLayout.fxml"));
            loader.setControllerFactory(injector::getInstance);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;

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
