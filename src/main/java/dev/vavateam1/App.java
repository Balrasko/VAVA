package dev.vavateam1;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger logger = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) throws Exception {
        // https://docs.oracle.com/javafx/2/get_started/fxml_tutorial.htm
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 640, 480);

        stage.setTitle("Kitchen management system");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        logger.log(Level.INFO, "Starting application");
        launch();
    }

}