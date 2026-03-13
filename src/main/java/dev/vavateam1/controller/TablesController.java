package dev.vavateam1.controller;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;

import javafx.scene.shape.Circle;

import java.math.BigDecimal;
import java.util.List;

import dev.vavateam1.model.Table;
import dev.vavateam1.service.TableService;

public class TablesController {

    @FXML
    private Pane tablesPane;

    private DashboardController dashboard;

    @FXML
    private Button dragButton;

    private final TableService tableService;

    @Inject
    public TablesController(TableService tableService) {
        this.tableService = tableService;
    }

    private List<Table> tables;

    private Boolean dragging = false;

    @FXML
    public void initialize() {

        // Get list of tables
        tables = tableService.getTables();

        // Make a node for each table
        if (!tables.isEmpty()) {
            for (Table table : tables) {
                if (table.getLocationId() == 1) { // Location check - nothing so far
                    Node node = createTableNode(table);
                    tablesPane.getChildren().add(node);
                }
            }
        }
    }

    public void setDashboardController(DashboardController dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    private void toggleDragging() {
        dragging = !dragging;

        dragButton.setText(dragging ? "Exit Drag Mode" : "Enter Drag Mode");
        dragButton.setStyle("-fx-font-size: 14px; -fx-border-color:#000000;"); // + (dragging ?
                                                                               // "-fx-background-color:#fffa61" :
                                                                               // "-fx-background-color:#61d2ff")
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
                    -fx-border-radius: 3;
                    -fx-background-radius: 5;
                """);

        // Draw green circle if table is available
        Circle status = new Circle(6);
        // status.setStyle(table.getAvailability() ? "-fx-fill: LIMEGREEN;" : "-fx-fill:
        // RED");
        status.setStyle("-fx-fill: LIMEGREEN");
        status.setVisible(table.getAvailability());

        box.getChildren().add(status);
        StackPane.setAlignment(status, Pos.TOP_RIGHT);
        StackPane.setMargin(status, new Insets(-4));

        enableTableDrag(box, table);
        box.setOnMouseClicked(e -> {
            if (!dragging) {
                try {
                    dashboard.showOrderView(table);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return box;
    }

    private double mouseX;
    private double mouseY;

    private void enableTableDrag(Node node, Table table) {

        node.setOnMousePressed(e -> {
            if (!dragging)
                return;

            mouseX = e.getSceneX() - node.getLayoutX();
            mouseY = e.getSceneY() - node.getLayoutY();
        });

        node.setOnMouseDragged(e -> {
            if (!dragging)
                return;

            node.setLayoutX(e.getSceneX() - mouseX);
            node.setLayoutY(e.getSceneY() - mouseY);

            // Don't let tables leave the screen
            if (node.getLayoutX() < 0) {
                node.setLayoutX(0);
            }
            if (node.getLayoutY() < 0) {
                node.setLayoutY(0);
            }
            if (node.getLayoutX() > tablesPane.getWidth() - node.getBoundsInParent().getWidth()) {
                node.setLayoutX(tablesPane.getWidth() - node.getBoundsInParent().getWidth());
            }
            if (node.getLayoutY() > tablesPane.getHeight() - node.getBoundsInParent().getHeight()) {
                node.setLayoutY(tablesPane.getHeight() - node.getBoundsInParent().getHeight());
            }
        });

        node.setOnMouseReleased(e -> {

            // Set new position
            table.setPosX(BigDecimal.valueOf(node.getLayoutX()));
            table.setPosY(BigDecimal.valueOf(node.getLayoutY()));

            // Save to DB - maybe could be done with its own button

            // System.out.println("X: " + table.getPosX());
            // System.out.println("Y: " + table.getPosY());
        });
    }
}
