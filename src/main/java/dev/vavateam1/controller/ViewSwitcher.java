package dev.vavateam1.controller;

import java.io.IOException;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import dev.vavateam1.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Singleton
public class ViewSwitcher {
    private Stage stage;
    private Injector injector;

    public void InitStage(Stage stage, Injector injector) throws IOException {
        this.stage = stage;
        this.injector = injector;

        SetView("/view/login.fxml");
        stage.setTitle("Restaurant Management System");
        stage.show();
    }

    public void SetView(String location) throws IOException {
        // TODO: only switch the scene root instead of loading a new scene
        FXMLLoader loader = new FXMLLoader(App.class.getResource(location));
        loader.setControllerFactory(injector::getInstance);
        Scene scene = new Scene(loader.load(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
    }
}
