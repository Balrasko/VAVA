package dev.vavateam1.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;

import dev.vavateam1.model.Table;
import dev.vavateam1.model.User;
import dev.vavateam1.service.AuthService;
import dev.vavateam1.util.I18n;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {
    private static final double SIDEBAR_OPEN_WIDTH = 208;
    private static final String VIEW_TABLES = "tables";
    private static final String VIEW_TABLE_LAYOUT = "tableLayout";
    private static final String VIEW_FINANCES = "finances";
    private static final String VIEW_INVENTORY = "inventory";
    private static final String VIEW_USERS = "users";
    private static final String VIEW_MENU = "menu";
    private static final String VIEW_CLOSING = "closing";
    private static final String VIEW_HISTORY = "history";
    private static final String VIEW_KITCHEN = "kitchen";

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
    private Label profileInitialsLabel;

    @FXML
    private Button languageButton;

    @FXML
    private HBox managerPanelItem;

    @FXML
    private HBox topNavbarContainer;

    private TopNavbarController topNavbarController;
    private TopNavbarZonesController topNavbarZonesController;
    private TablesController tablesController;
    private int activeZoneId = 1;
    private String currentViewKey = VIEW_TABLES;

    private boolean sidebarVisible = false;

    private boolean isAdmin = false;

    @FXML
    public void initialize() {
        User currentUser = authService.getUser();
        boolean isKitchenStaff = currentUser != null && currentUser.getRoleId() == 3;
        isAdmin = currentUser != null && currentUser.getRoleId() == 1;
        ViewSwitcher.DashboardState restoredState = viewSwitcher.consumeDashboardState();

        sidebar.setPrefWidth(0);
        sidebar.setMinWidth(0);
        sidebar.setMaxWidth(0);
        sidebarVisible = false;

        if (languageButton != null) {
            languageButton.setText(I18n.nextLanguageCode());
        }

        if (loggedInRoleLabel != null && currentUser != null) {
            loggedInRoleLabel.setText(getRoleName(currentUser.getRoleId()));
        }
        if (profileInitialsLabel != null && currentUser != null) {
            profileInitialsLabel.setText(getInitials(currentUser.getName()));
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
            } else if (restoredState != null) {
                activeZoneId = restoredState.activeZoneId();
                restoreView(restoredState.viewKey());
            } else {
                showTableView();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void switchLanguage() throws Exception {
        viewSwitcher.setDashboardState(new ViewSwitcher.DashboardState(currentViewKey, activeZoneId));
        I18n.toggleLocale();
        viewSwitcher.reloadCurrentView();
    }

    private void restoreView(String viewKey) throws Exception {
        switch (viewKey) {
            case VIEW_TABLE_LAYOUT -> showManagerView(VIEW_TABLE_LAYOUT);
            case VIEW_FINANCES -> showManagerView(VIEW_FINANCES);
            case VIEW_INVENTORY -> showManagerView(VIEW_INVENTORY);
            case VIEW_USERS -> showManagerView(VIEW_USERS);
            case VIEW_MENU -> showManagerView(VIEW_MENU);
            case VIEW_CLOSING -> showClosing();
            case VIEW_HISTORY -> showHistory();
            default -> showTableView();
        }
    }

    private void showManagerView(String tabName) throws Exception {
        if (!isAdmin) {
            showTableView();
            return;
        }

        loadManagerTopNavbar();
        if (topNavbarController != null) {
            topNavbarController.setActiveTab(tabName);
        }

        switch (tabName) {
            case VIEW_FINANCES -> showFinances();
            case VIEW_INVENTORY -> showInventory();
            case VIEW_USERS -> showUsers();
            case VIEW_MENU -> showMenu();
            default -> showTableLayout();
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
        FXMLLoader loader = I18n.loader(getClass().getResource("/view/top-navbar.fxml"), injector);

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
        FXMLLoader loader = I18n.loader(getClass().getResource("/view/top-navbar-zones.fxml"), injector);

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

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    @FXML
    public void showTableView() throws Exception {
        FXMLLoader loader = I18n.loader(
                getClass().getResource("/view/tables.fxml"), injector);

        Parent view = loader.load();

        tablesController = loader.getController();
        tablesController.setDashboardController(this);
        tablesController.setActiveZone(activeZoneId);

        contentArea.getChildren().setAll(view);
        currentViewKey = VIEW_TABLES;

        loadZonesTopNavbar();
    }

    @FXML
    public void showOrderView(Table table) throws Exception {
        FXMLLoader loader = I18n.loader(
                getClass().getResource("/view/order.fxml"), injector);

        Parent view = loader.load();

        OrderController controller = loader.getController();
        controller.initData(table, this);

        contentArea.getChildren().setAll(view);
        setTopNavbarVisible(false);
    }

    private void showKitchenView() throws Exception {
        FXMLLoader loader = I18n.loader(
                getClass().getResource("/view/kitchen.fxml"), injector);

        Parent view = loader.load();
        contentArea.getChildren().setAll(view);
        currentViewKey = VIEW_KITCHEN;
    }

    @FXML
    private void showClosing() {
        try {

            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/closing.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            currentViewKey = VIEW_CLOSING;
            setTopNavbarVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMenu() {
        try {
            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/managermenu.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            currentViewKey = VIEW_MENU;

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
            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/users.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            currentViewKey = VIEW_USERS;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHistory() {

        try {

            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/history.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            currentViewKey = VIEW_HISTORY;
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
            currentViewKey = VIEW_TABLE_LAYOUT;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showFinances() {
        try {

            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/finances.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            currentViewKey = VIEW_FINANCES;

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

            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/inventory.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            currentViewKey = VIEW_INVENTORY;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTableLayout() {
        try {

            FXMLLoader loader = I18n.loader(
                    getClass().getResource("/view/tableLayout.fxml"), injector);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());
            tablesController = null;
            currentViewKey = VIEW_TABLE_LAYOUT;

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
            case 1 -> I18n.t("role.admin");
            case 2 -> I18n.t("role.waiter");
            case 3 -> I18n.t("role.chef");
            default -> I18n.t("role.user");
        };
    }
}
