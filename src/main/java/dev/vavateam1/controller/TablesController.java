package dev.vavateam1.controller;

import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.model.Table;
import dev.vavateam1.service.TableService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

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
    private int activeZoneId = 1;

    private Boolean dragging = false;

    @FXML
    public void initialize() {

        // Get list of tables
        tables = tableService.getTables();

        renderTables();
    }

    public void setActiveZone(int zoneId) {
        activeZoneId = zoneId;
        renderTables();
    }

    private void renderTables() {
        if (tablesPane == null || tables == null) {
            return;
        }

        tablesPane.getChildren().clear();

        // Make a node for each table
        if (!tables.isEmpty()) {
            for (Table table : tables) {
                if (table.getLocationId() == activeZoneId) {
                    Node node = createTableNode(table);
                    tablesPane.getChildren().add(node);
                }
            }
        }
    }

    public void setDashboardController(DashboardController dashboard) {
        this.dashboard = dashboard;
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
}
