package dev.vavateam1.controller;

import java.io.IOException;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import dev.vavateam1.App;
import dev.vavateam1.model.Table;
import dev.vavateam1.util.I18n;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Singleton
public class ViewSwitcher {
    public record DashboardState(String viewKey, int activeZoneId, Table activeOrderTable) {
        public DashboardState(String viewKey, int activeZoneId) {
            this(viewKey, activeZoneId, null);
        }
    }

    private Stage stage;
    private Injector injector;
    private String currentLocation;
    private DashboardState dashboardState;

    public void InitStage(Stage stage, Injector injector) throws IOException {
        this.stage = stage;
        this.injector = injector;

        SetView("/view/login.fxml");
        stage.setTitle(I18n.t("app.title"));
        stage.show();
    }

    public void SetView(String location) throws IOException {
        currentLocation = location;
        var loader = I18n.loader(App.class.getResource(location), injector);
        Parent root = loader.load();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle(I18n.t("app.title"));
    }

    public void reloadCurrentView() throws IOException {
        SetView(currentLocation != null ? currentLocation : "/view/login.fxml");
    }

    public void setDashboardState(DashboardState dashboardState) {
        this.dashboardState = dashboardState;
    }

    public DashboardState consumeDashboardState() {
        DashboardState state = dashboardState;
        dashboardState = null;
        return state;
    }
}
