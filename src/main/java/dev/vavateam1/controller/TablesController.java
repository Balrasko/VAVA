package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.Node;

import javafx.scene.shape.Circle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.List;

import dev.vavateam1.controller.DashboardController.*;
import dev.vavateam1.model.Table;
import dev.vavateam1.service.*;

public class TablesController {
    
    @FXML
    private StackPane contentArea;

    @FXML
    private Pane tablesPane;

    // Mock backend service
    private final TableService tableService = new MockTableService();

    private List<Table> tables;


    @FXML
    public void initialize() {
        
        // Get list of tables
        tables = tableService.getTables();

        // Make a node for each table
        if (!tables.isEmpty()) {
            for (Table table : tables) {
                Node node = createTableNode(table);
                tablesPane.getChildren().add(node);
            }
        }
    }


    private Node createTableNode(Table table) {
        StackPane box = new StackPane();
        box.setPrefSize(160, 80);
    
        Label label = new Label("Table " + table.getTableNumber());

        box.getChildren().add(label);

        box.setLayoutX(table.getPosX().doubleValue());
        box.setLayoutY(table.getPosY().doubleValue());

        box.setStyle("""
            -fx-background-color: lightblue;
            -fx-border-color: black;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
        """);

        Circle status = new Circle(6);
        // status.setStyle(table.getAvailability() ? "-fx-fill: LIMEGREEN;" : "-fx-fill: RED");
        status.setStyle("-fx-fill: LIMEGREEN");
        status.setVisible(table.getAvailability());

        box.getChildren().add(status);
        StackPane.setAlignment(status, Pos.TOP_RIGHT);
        StackPane.setMargin(status, new Insets(4));

        enableDrag(box, table);

        return box;
    }

    private double mouseX;
    private double mouseY;

    private void enableDrag(Node node, Table table) {

        node.setOnMousePressed(e -> {
            mouseX = e.getSceneX() - node.getLayoutX();
            mouseY = e.getSceneY() - node.getLayoutY();
        });

        node.setOnMouseDragged(e -> {
            node.setLayoutX(e.getSceneX() - mouseX);
            node.setLayoutY(e.getSceneY() - mouseY);
        });

        node.setOnMouseReleased(e -> {
            table.setPosX(BigDecimal.valueOf(node.getLayoutX()));
            table.setPosY(BigDecimal.valueOf(node.getLayoutY()));

            // System.out.println("X: " + table.getPosX());
            // System.out.println("Y: " + table.getPosY());
        });
    }
}
