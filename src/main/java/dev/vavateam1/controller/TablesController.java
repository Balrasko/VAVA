package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import dev.vavateam1.controller.DashboardController.*;

public class TablesController {
    
    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new Label("Table screen initialized."));
    }
}
