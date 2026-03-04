package dev.vavateam1.controller;

import com.google.inject.Inject;
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
    private void showTables() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/tables.fxml")
        );

        Parent view = loader.load();

        contentArea.getChildren().setAll(view);
    }

    @FXML
    private void showClosing() {
        setContent("Here will be Closing screen");
    }

    @FXML
    private void showHistory() {
        setContent("Here will be History screen");
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
