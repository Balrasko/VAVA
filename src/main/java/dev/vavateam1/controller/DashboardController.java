package dev.vavateam1.controller;

import com.google.inject.Inject;

import dev.vavateam1.model.Table;
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
import javafx.fxml.FXMLLoader;

public class DashboardController {
    private final AuthService authService;
    private final ViewSwitcher viewSwitcher;

    @Inject
    public DashboardController(AuthService authService, ViewSwitcher viewSwitcher) {
        this.authService = authService;
        this.viewSwitcher = viewSwitcher;
    }

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebar;

    private boolean sidebarVisible = false;

    @FXML
    public void initialize() {
        sidebar.setPrefWidth(0);
        sidebar.setMinWidth(0);
        sidebar.setMaxWidth(0);
        sidebarVisible = false;

        try {
            showTableView();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            showTableView();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void toggleSidebar() {

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

    @FXML
    public void showTableView() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/tables.fxml")
        );

        Parent view = loader.load();

        TablesController controller = loader.getController();
        controller.setDashboardController(this);

        contentArea.getChildren().setAll(view);
    }

    @FXML
    public void showOrderView(Table table) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/view/order.fxml")
        );

        Parent view = loader.load();

        OrderController controller = loader.getController();
        controller.initData(table, this);

        contentArea.getChildren().setAll(view);
    }

    @FXML
    private void showClosing() {
        setContent("Here will be Closing screen");
    }

    @FXML
    private void showHistory() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/history.fxml")
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(loader.load());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void showManager() {
        setContent("Here will be Manager panel");
    }

    @FXML
    private void logout() throws Exception {
        authService.logout();
        viewSwitcher.SetView("/view/login.fxml");
    }
}
