package dev.vavateam1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 800);

        scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        stage.setTitle("Restaurant Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}