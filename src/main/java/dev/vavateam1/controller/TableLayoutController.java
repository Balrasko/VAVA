package dev.vavateam1.controller;

import com.google.inject.Inject;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.vavateam1.model.Table;
import dev.vavateam1.service.TableService;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class TableLayoutController {

    @FXML
    private Pane tablesPane;

    @FXML
    private TopNavbarZonesController zonesNavbarController;

    private DashboardController dashboard;

    @FXML
    private Button dragButton;

    @FXML
    private Button undoButton;

    @FXML
    private Button addButton;

    private Popup addPopup;

    private final TableService tableService;

    @Inject
    public TableLayoutController(TableService tableService) {
        this.tableService = tableService;
    }

    private List<Table> tables;
    private int activeZoneId = 1;

    private Boolean dragging = false;
    private final Map<Integer, TablePosition> originalPositions = new HashMap<>();
    private final Map<Integer, Node> tableNodesById = new HashMap<>();

    @FXML
    public void initialize() {

        // Get list of tables
        tables = tableService.getTables();

        if (zonesNavbarController != null) {
            zonesNavbarController.setOnZoneSelected(this::setActiveZone);
            zonesNavbarController.setActiveZone(activeZoneId);
            zonesNavbarController.setEditMode(false);
        }

        renderTables();
        setUndoButtonState(false);
        buildAddPopup();
    }

    private void setActiveZone(int zoneId) {
        activeZoneId = zoneId;
        renderTables();
    }

    private void renderTables() {
        if (tablesPane == null || tables == null) {
            return;
        }

        tablesPane.getChildren().clear();
        tableNodesById.clear();

        if (!tables.isEmpty()) {
            for (Table table : tables) {
                if (table.getLocationId() == activeZoneId) {
                    Node node = createTableNode(table);
                    tablesPane.getChildren().add(node);
                    tableNodesById.put(table.getId(), node);
                }
            }
        }
    }

    @FXML
    private void toggleDragging() {
        if (!dragging) {
            beginDragMode();
        } else {
            saveTablePositions();
            endDragMode();
        }
    }

    private void beginDragMode() {
        dragging = true;
        snapshotOriginalPositions();
        dragButton.setText("Exit Edit Mode");
        dragButton.setStyle(
                "-fx-background-color:#57c84d; -fx-text-fill:#1d1d1d; -fx-background-radius:24; -fx-padding:12 24 12 24; -fx-cursor:hand;");
        setUndoButtonState(true);
        setAddButtonState(true);
        if (zonesNavbarController != null) {
            zonesNavbarController.setEditMode(true);
        }
        renderTables();
    }

    private void endDragMode() {
        dragging = false;
        dragButton.setText("Enter Edit Mode");
        dragButton.setStyle(
                "-fx-background-color:#57c84d; -fx-text-fill:#1d1d1d; -fx-background-radius:24; -fx-padding:12 24 12 24; -fx-cursor:hand;");
        setUndoButtonState(false);
        setAddButtonState(false);
        if (zonesNavbarController != null) {
            zonesNavbarController.setEditMode(false);
        }
        if (addPopup != null)
            addPopup.hide();
        originalPositions.clear();
        renderTables();
    }

    private void setAddButtonState(boolean enabled) {
        if (addButton != null) {
            addButton.setVisible(enabled);
            addButton.setManaged(enabled);
        }
    }

    private void buildAddPopup() {
        addPopup = new Popup();
        addPopup.setAutoHide(true);

        Button newTableBtn = new Button("New Table");
        Button newZoneBtn = new Button("New Zone");

        String itemStyle = "-fx-background-color: white; -fx-text-fill: #1d1d1d; -fx-font-size: 14px; "
                + "-fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20; -fx-pref-width: 180px; "
                + "-fx-cursor: hand; -fx-border-color: transparent;";
        String itemHover = "-fx-background-color: #f0f0f0; -fx-text-fill: #1d1d1d; -fx-font-size: 14px; "
                + "-fx-alignment: CENTER_LEFT; -fx-padding: 10 20 10 20; -fx-pref-width: 180px; "
                + "-fx-cursor: hand; -fx-border-color: transparent;";

        newTableBtn.setStyle(itemStyle);
        newZoneBtn.setStyle(itemStyle);

        newTableBtn.setOnMouseEntered(e -> newTableBtn.setStyle(itemHover));
        newTableBtn.setOnMouseExited(e -> newTableBtn.setStyle(itemStyle));
        newZoneBtn.setOnMouseEntered(e -> newZoneBtn.setStyle(itemHover));
        newZoneBtn.setOnMouseExited(e -> newZoneBtn.setStyle(itemStyle));

        newTableBtn.setOnAction(e -> {
            addPopup.hide();
            onCreateNewTable();
        });
        newZoneBtn.setOnAction(e -> {
            addPopup.hide();
            onCreateNewZone();
        });

        VBox menu = new VBox(newTableBtn, newZoneBtn);
        menu.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-radius: 8; -fx-border-color: #d0d0d0; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

        addPopup.getContent().add(menu);
    }

    @FXML
    private void onAddClicked() {
        if (addPopup == null || addButton == null)
            return;
        if (addPopup.isShowing()) {
            addPopup.hide();
            return;
        }
        javafx.geometry.Bounds bounds = addButton.localToScreen(addButton.getBoundsInLocal());
        addPopup.show(addButton, bounds.getMinX() - 136, bounds.getMaxY() + 4);
    }

    private void onCreateNewTable() {
        Table newTable = tableService.createTable(activeZoneId);
        if (newTable != null) {
            tables.add(newTable);
            renderTables();
        }
    }

    private void onCreateNewZone() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Zone");
        dialog.setHeaderText("Create a new zone");
        dialog.setContentText("Please enter the name of the new zone:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                dev.vavateam1.model.Location newZone = tableService.createZone(name.trim());
                if (zonesNavbarController != null && newZone != null) {
                    zonesNavbarController.loadZones();
                }
            }
        });
    }

    private void setUndoButtonState(boolean enabled) {
        if (undoButton != null) {
            undoButton.setVisible(enabled);
            undoButton.setManaged(enabled);
            undoButton.setDisable(!enabled);
        }
    }

    private void snapshotOriginalPositions() {
        originalPositions.clear();
        for (Table table : tables) {
            if (table.getLocationId() == activeZoneId) {
                originalPositions.put(table.getId(), new TablePosition(table.getPosX(), table.getPosY()));
            }
        }
    }

    @FXML
    private void undoTableLayoutChanges() {
        if (!dragging || originalPositions.isEmpty()) {
            return;
        }

        for (Table table : tables) {
            TablePosition original = originalPositions.get(table.getId());
            if (original == null) {
                continue;
            }

            table.setPosX(original.posX());
            table.setPosY(original.posY());

            Node node = tableNodesById.get(table.getId());
            if (node != null) {
                node.setLayoutX(original.posX().doubleValue());
                node.setLayoutY(original.posY().doubleValue());
            }
        }
    }

    private void saveTablePositions() {
        for (Table table : tables) {
            if (table.getLocationId() == activeZoneId) {
                tableService.updateTablePosition(table.getId(), table.getPosX(), table.getPosY());
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
                    -fx-border-radius: 3;
                    -fx-background-radius: 5;
                """);

        Circle status = new Circle(6);
        status.setStyle("-fx-fill: LIMEGREEN");
        status.setVisible(!dragging && table.getAvailability());

        Button deleteMarker = new Button("X");
        deleteMarker.setStyle("-fx-background-color:#d62828; -fx-text-fill:#e8e8e8; -fx-font-size:14px; "
                + "-fx-font-weight:900; -fx-font-family:'Arial'; -fx-cursor:hand; -fx-padding:0 0 1 0; "
                + "-fx-background-radius:999; -fx-border-radius:999; -fx-min-width:20px; -fx-min-height:20px; "
                + "-fx-max-width:20px; -fx-max-height:20px; -fx-alignment:center; -fx-border-color:transparent; -fx-border-color: #7f1d1d; -fx-border-width: 1");
        deleteMarker.setVisible(dragging);
        deleteMarker.setOnAction(e -> {
            if (!dragging) {
                return;
            }
            System.out.println("Delete table " + table.getTableNumber());
        });

        box.getChildren().add(status);
        box.getChildren().add(deleteMarker);
        StackPane.setAlignment(status, Pos.TOP_RIGHT);
        StackPane.setMargin(status, new Insets(-4));
        StackPane.setAlignment(deleteMarker, Pos.TOP_RIGHT);
        StackPane.setMargin(deleteMarker, new Insets(-8, -8, 0, 0));

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

    private record TablePosition(BigDecimal posX, BigDecimal posY) {
    }
}
