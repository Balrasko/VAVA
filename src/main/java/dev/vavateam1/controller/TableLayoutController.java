package dev.vavateam1.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import dev.vavateam1.model.Location;
import dev.vavateam1.model.Table;
import dev.vavateam1.service.TableService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class TableLayoutController {

    private enum FormMode {
        NONE,
        ADD_TABLE,
        ADD_ZONE,
        EDIT_TABLE,
        EDIT_ZONE
    }

    @FXML
    private Pane tablesPane;

    @FXML
    private TopNavbarZonesController zonesNavbarController;

    @FXML
    private Button dragButton;

    @FXML
    private Button editCancelButton;

    @FXML
    private Button addTableButton;
    @FXML
    private Button editZoneButton;
    @FXML
    private Button deleteZoneButton;

    @FXML
    private VBox layoutFormPanel;

    @FXML
    private Label layoutFormTitle;

    @FXML
    private Label layoutFormHint;

    @FXML
    private VBox tableFieldsContainer;

    @FXML
    private VBox zoneFieldsContainer;

    @FXML
    private TextField tableNumberField;

    @FXML
    private CheckBox tableAvailabilityCheck;

    @FXML
    private ComboBox<ZoneOption> tableZoneComboBox;

    @FXML
    private TextField zoneNameField;

    @FXML
    private Button submitLayoutButton;

    @FXML
    private Button deleteLayoutButton;

    private DashboardController dashboard;

    private final TableService tableService;

    private List<Table> tables;
    private int activeZoneId = 1;
    private boolean dragging = false;
    private FormMode formMode = FormMode.NONE;
    private Table selectedTable;
    private Location selectedZone;

    private Map<Integer, String> locationNamesById;
    private Set<Integer> tablesWithUnpaidItems = Collections.emptySet();
    private final Map<Integer, TablePosition> originalPositions = new HashMap<>();
    private final Map<Integer, Node> tableNodesById = new HashMap<>();
    private final Set<Integer> pendingDeletedTableIds = new HashSet<>();

    private double mouseX;
    private double mouseY;

    @Inject
    public TableLayoutController(TableService tableService) {
        this.tableService = tableService;
    }

    @FXML
    public void initialize() {
        tables = tableService.getTables();
        locationNamesById = tableService.getLocations().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));
        tablesWithUnpaidItems = tableService.getTablesWithUnpaidItems();

        if (zonesNavbarController != null) {
            zonesNavbarController.setAddZoneTabVisible(true);
            zonesNavbarController.setOnZoneSelected(this::setActiveZone);
            zonesNavbarController.setOnAddZoneRequested(this::onOpenAddZoneForm);
            zonesNavbarController.setActiveZone(activeZoneId);
        }

        renderTables();
        setEditModeActionButtonsVisible(false);
        setAddTableButtonVisible(true);
        hideForm();
    }

    private void setActiveZone(int zoneId) {
        activeZoneId = zoneId;
        updateZoneActionButtonsState();
        renderTables();
    }

    private void renderTables() {
        if (tablesPane == null || tables == null) {
            return;
        }

        tablesPane.getChildren().clear();
        tableNodesById.clear();

        for (Table table : tables) {
            if (pendingDeletedTableIds.contains(table.getId())) {
                continue;
            }
            if (table.getLocationId() == activeZoneId) {
                Node node = createTableNode(table);
                tablesPane.getChildren().add(node);
                tableNodesById.put(table.getId(), node);
            }
        }
    }

    @FXML
    private void toggleDragging() {
        if (!dragging) {
            beginDragMode();
            return;
        }

        saveAndExitTableEditMode();
    }

    private void beginDragMode() {
        dragging = true;
        snapshotOriginalPositions();
        pendingDeletedTableIds.clear();
        dragButton.setText("Save");
        setEditModeActionButtonsVisible(true);
        setAddTableButtonVisible(false);
        setZoneActionButtonsVisible(false);
        hideForm();
        renderTables();
    }

    private void endDragMode() {
        dragging = false;
        dragButton.setText("Enter Edit Mode");
        setEditModeActionButtonsVisible(false);
        setAddTableButtonVisible(true);
        setZoneActionButtonsVisible(true);
        originalPositions.clear();
        pendingDeletedTableIds.clear();
        renderTables();
    }

    private void setEditModeActionButtonsVisible(boolean visible) {
        if (editCancelButton != null) {
            editCancelButton.setVisible(visible);
            editCancelButton.setManaged(visible);
        }
    }

    @FXML
    private void onCancelTableEditMode() {
        if (!dragging) {
            return;
        }

        revertTablePositions();
        endDragMode();
    }

    private void saveAndExitTableEditMode() {
        persistStagedTableChanges();
        reloadTables();
        endDragMode();
    }

    private void setAddTableButtonVisible(boolean visible) {
        if (addTableButton != null) {
            addTableButton.setVisible(visible);
            addTableButton.setManaged(visible);
        }
    }

    private void setZoneActionButtonsVisible(boolean visible) {
        if (editZoneButton != null) {
            editZoneButton.setVisible(visible);
            editZoneButton.setManaged(visible);
        }

        if (deleteZoneButton != null) {
            deleteZoneButton.setVisible(visible);
            deleteZoneButton.setManaged(visible);
        }
    }

    private void updateZoneActionButtonsState() {
        boolean hasActiveZone = activeZoneId > 0;
        if (editZoneButton != null) {
            editZoneButton.setDisable(!hasActiveZone);
        }
        if (deleteZoneButton != null) {
            deleteZoneButton.setDisable(!hasActiveZone);
        }
    }

    @FXML
    private void onEditCurrentZone() {
        if (activeZoneId <= 0) {
            return;
        }

        Location activeZone = tableService.getLocations().stream()
                .filter(zone -> zone.getId() == activeZoneId)
                .findFirst()
                .orElse(null);
        if (activeZone == null) {
            return;
        }

        onOpenEditZoneForm(activeZone);
    }

    @FXML
    private void onDeleteCurrentZone() {
        if (activeZoneId <= 0) {
            return;
        }
        onDeleteZoneById(activeZoneId);
    }

    @FXML
    private void onOpenAddTableForm() {
        formMode = FormMode.ADD_TABLE;
        selectedTable = null;
        selectedZone = null;

        layoutFormTitle.setText("Add table");
        layoutFormHint.setText("Create a table in the active zone.");
        submitLayoutButton.setText("Add table");

        showTableFields(true);
        showZoneFields(false);
        setDeleteButtonVisible(false);

        loadZoneOptions(activeZoneId);
        tableNumberField.clear();
        tableAvailabilityCheck.setSelected(true);
        showForm();
    }

    @FXML
    private void onOpenAddZoneForm() {
        formMode = FormMode.ADD_ZONE;
        selectedTable = null;
        selectedZone = null;

        layoutFormTitle.setText("Add zone");
        layoutFormHint.setText("Create a new zone tab.");
        submitLayoutButton.setText("Add zone");

        showTableFields(false);
        showZoneFields(true);
        setDeleteButtonVisible(false);

        zoneNameField.clear();
        showForm();
    }

    private void onOpenEditZoneForm(Location zone) {
        formMode = FormMode.EDIT_ZONE;
        selectedTable = null;
        selectedZone = zone;

        layoutFormTitle.setText("Edit zone");
        layoutFormHint.setText("Editing zone " + zone.getName() + ".");
        submitLayoutButton.setText("Save zone");

        showTableFields(false);
        showZoneFields(true);
        setDeleteButtonVisible(true);

        zoneNameField.setText(zone.getName());
        showForm();
    }

    @FXML
    private void onCloseLayoutForm() {
        hideForm();
    }

    private void showForm() {
        layoutFormPanel.setVisible(true);
        layoutFormPanel.setManaged(true);
    }

    private void hideForm() {
        layoutFormPanel.setVisible(false);
        layoutFormPanel.setManaged(false);
        formMode = FormMode.NONE;
        selectedTable = null;
        selectedZone = null;
        setDeleteButtonVisible(false);
    }

    private void setDeleteButtonVisible(boolean visible) {
        if (deleteLayoutButton != null) {
            deleteLayoutButton.setVisible(visible);
            deleteLayoutButton.setManaged(visible);
        }
    }

    private void showTableFields(boolean visible) {
        tableFieldsContainer.setVisible(visible);
        tableFieldsContainer.setManaged(visible);
    }

    private void showZoneFields(boolean visible) {
        zoneFieldsContainer.setVisible(visible);
        zoneFieldsContainer.setManaged(visible);
    }

    @FXML
    private void onSubmitLayoutForm() {
        switch (formMode) {
            case ADD_TABLE -> submitAddTable();
            case ADD_ZONE -> submitAddZone();
            case EDIT_TABLE -> submitEditTable();
            case EDIT_ZONE -> submitEditZone();
            case NONE -> {
                return;
            }
        }

        hideForm();
        reloadTables();
        renderTables();
    }

    private void submitAddTable() {
        int targetZoneId = resolveSelectedZoneId(activeZoneId);
        Table newTable = tableService.createTable(targetZoneId);
        if (newTable == null) {
            return;
        }

        Integer parsedNumber = parseTableNumber(tableNumberField.getText(), false);
        if (parsedNumber != null) {
            newTable.setTableNumber(parsedNumber);
        }

        boolean available = tableAvailabilityCheck.isSelected();
        newTable.setAvailability(available);
        newTable.setLocationId(targetZoneId);

        tableService.updateTableDetails(newTable.getId(), targetZoneId, newTable.getTableNumber(), available);

        TablePosition targetPosition = findAvailablePosition(targetZoneId, newTable.getId());
        newTable.setPosX(targetPosition.posX());
        newTable.setPosY(targetPosition.posY());
        tableService.updateTablePosition(newTable.getId(), targetPosition.posX(), targetPosition.posY());
    }

    private void submitAddZone() {
        String zoneName = zoneNameField.getText() != null ? zoneNameField.getText().trim() : "";
        if (zoneName.isEmpty()) {
            return;
        }

        Location newZone = tableService.createZone(zoneName);
        if (newZone != null) {
            reloadZonesAndSelect(newZone.getId());
        }
    }

    private void submitEditZone() {
        if (selectedZone == null) {
            return;
        }

        String zoneName = zoneNameField.getText() != null ? zoneNameField.getText().trim() : "";
        if (zoneName.isEmpty()) {
            return;
        }

        tableService.updateZoneName(selectedZone.getId(), zoneName);
        reloadZonesAndSelect(selectedZone.getId());
    }

    private void submitEditTable() {
        if (selectedTable == null) {
            return;
        }

        Integer parsedNumber = parseTableNumber(tableNumberField.getText(), true);
        if (parsedNumber == null) {
            return;
        }

        int sourceZoneId = selectedTable.getLocationId();
        int targetZoneId = resolveSelectedZoneId(selectedTable.getLocationId());
        boolean available = tableAvailabilityCheck.isSelected();
        tableService.updateTableDetails(selectedTable.getId(), targetZoneId, parsedNumber, available);

        selectedTable.setLocationId(targetZoneId);
        selectedTable.setTableNumber(parsedNumber);
        selectedTable.setAvailability(available);

        if (sourceZoneId != targetZoneId) {
            TablePosition targetPosition = findAvailablePosition(targetZoneId, selectedTable.getId());
            selectedTable.setPosX(targetPosition.posX());
            selectedTable.setPosY(targetPosition.posY());
            tableService.updateTablePosition(selectedTable.getId(), targetPosition.posX(), targetPosition.posY());
        }
    }

    private void reloadTables() {
        tables = tableService.getTables();
        locationNamesById = tableService.getLocations().stream()
                .collect(Collectors.toMap(Location::getId, Location::getName));
        tablesWithUnpaidItems = tableService.getTablesWithUnpaidItems();
    }

    private TablePosition findAvailablePosition(int zoneId, int movingTableId) {
        final double width = 160;
        final double height = 80;
        final double gapX = 24;
        final double gapY = 24;
        final int maxColumns = 5;

        int row = 0;
        int col = 0;
        while (row < 100) {
            double x = col * (width + gapX);
            double y = row * (height + gapY);

            boolean occupied = false;
            for (Table table : tables) {
                if (table.getId() == movingTableId || table.getLocationId() != zoneId) {
                    continue;
                }

                if (table.getPosX() == null || table.getPosY() == null) {
                    continue;
                }

                if (Math.abs(table.getPosX().doubleValue() - x) < 1
                        && Math.abs(table.getPosY().doubleValue() - y) < 1) {
                    occupied = true;
                    break;
                }
            }

            if (!occupied) {
                return new TablePosition(BigDecimal.valueOf(x), BigDecimal.valueOf(y));
            }

            col++;
            if (col >= maxColumns) {
                col = 0;
                row++;
            }
        }

        return new TablePosition(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private void loadZoneOptions(int selectedZoneId) {
        if (tableZoneComboBox == null) {
            return;
        }

        List<Location> zones = tableService.getLocations();
        List<ZoneOption> options = new ArrayList<>();
        for (Location zone : zones) {
            options.add(new ZoneOption(zone.getId(), zone.getName()));
        }

        tableZoneComboBox.getItems().setAll(options);

        ZoneOption selected = options.stream()
                .filter(option -> option.id() == selectedZoneId)
                .findFirst()
                .orElse(options.isEmpty() ? null : options.get(0));
        tableZoneComboBox.getSelectionModel().select(selected);
    }

    private int resolveSelectedZoneId(int fallbackZoneId) {
        if (tableZoneComboBox == null || tableZoneComboBox.getSelectionModel() == null) {
            return fallbackZoneId;
        }

        ZoneOption selectedOption = tableZoneComboBox.getSelectionModel().getSelectedItem();
        return selectedOption != null ? selectedOption.id() : fallbackZoneId;
    }

    private Integer parseTableNumber(String value, boolean required) {
        String normalized = value != null ? value.trim() : "";
        if (normalized.isEmpty()) {
            return required ? null : null;
        }

        try {
            int number = Integer.parseInt(normalized);
            return number > 0 ? number : null;
        } catch (NumberFormatException ex) {
            return null;
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

    private void revertTablePositions() {
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

    private void persistStagedTableChanges() {
        for (Table table : tables) {
            if (pendingDeletedTableIds.contains(table.getId())) {
                tableService.softDeleteTable(table.getId());
                continue;
            }

            if (table.getLocationId() == activeZoneId && originalPositions.containsKey(table.getId())) {
                tableService.updateTablePosition(table.getId(), table.getPosX(), table.getPosY());
            }
        }
    }

    private Node createTableNode(Table table) {
        StackPane box = new StackPane();
        box.setPrefSize(160, 80);
        box.getStyleClass().add("table-card");

        String locationName = locationNamesById.getOrDefault(table.getLocationId(), "Zone");
        Label label = new Label(locationName + " " + table.getTableNumber());
        label.getStyleClass().add("table-card-label");
        box.getChildren().add(label);

        box.setLayoutX(table.getPosX().doubleValue());
        box.setLayoutY(table.getPosY().doubleValue());

        Circle status = new Circle(6);
        status.getStyleClass().add("table-status-dot");
        boolean hasUnpaid = tablesWithUnpaidItems.contains(table.getId());
        status.setStyle(hasUnpaid ? "-fx-fill: RED" : "-fx-fill: LIMEGREEN");
        status.setVisible(!dragging);

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
            pendingDeletedTableIds.add(table.getId());
            renderTables();
        });

        box.getChildren().add(status);
        box.getChildren().add(deleteMarker);
        StackPane.setAlignment(status, Pos.TOP_RIGHT);
        StackPane.setMargin(status, new Insets(-4));
        StackPane.setAlignment(deleteMarker, Pos.TOP_RIGHT);
        StackPane.setMargin(deleteMarker, new Insets(-8, -8, 0, 0));

        enableTableDrag(box, table);

        return box;
    }

    private void enableTableDrag(Node node, Table table) {
        final double[] pressSceneX = new double[1];
        final double[] pressSceneY = new double[1];
        final boolean[] pointerMoved = new boolean[1];

        node.setOnMousePressed(e -> {
            pressSceneX[0] = e.getSceneX();
            pressSceneY[0] = e.getSceneY();
            pointerMoved[0] = false;

            if (!dragging) {
                return;
            }
            mouseX = e.getSceneX() - node.getLayoutX();
            mouseY = e.getSceneY() - node.getLayoutY();
        });

        node.setOnMouseDragged(e -> {
            if (Math.abs(e.getSceneX() - pressSceneX[0]) > 2 || Math.abs(e.getSceneY() - pressSceneY[0]) > 2) {
                pointerMoved[0] = true;
            }

            if (!dragging) {
                return;
            }

            node.setLayoutX(e.getSceneX() - mouseX);
            node.setLayoutY(e.getSceneY() - mouseY);

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
            if (!dragging) {
                if (!pointerMoved[0]) {
                    openEditTableFor(table);
                }
                return;
            }

            table.setPosX(BigDecimal.valueOf(node.getLayoutX()));
            table.setPosY(BigDecimal.valueOf(node.getLayoutY()));
        });
    }

    private void openEditTableFor(Table table) {
        formMode = FormMode.EDIT_TABLE;
        selectedTable = table;
        selectedZone = null;

        layoutFormTitle.setText("Edit table");
        layoutFormHint.setText("Editing table " + table.getTableNumber() + ".");
        submitLayoutButton.setText("Save table");

        showTableFields(true);
        showZoneFields(false);
        setDeleteButtonVisible(false);

        loadZoneOptions(table.getLocationId());
        tableNumberField.setText(String.valueOf(table.getTableNumber()));
        tableAvailabilityCheck.setSelected(Boolean.TRUE.equals(table.getAvailability()));
        showForm();
    }

    @FXML
    private void onDeleteLayoutEntity() {
        if (formMode != FormMode.EDIT_ZONE || selectedZone == null) {
            return;
        }

        onDeleteZoneById(selectedZone.getId());
        hideForm();
    }

    private void onDeleteZoneById(int zoneId) {
        tableService.softDeleteZone(zoneId);

        int fallbackZoneId = resolveFallbackZoneId(zoneId);
        reloadZonesAndSelect(fallbackZoneId);
    }

    private int resolveFallbackZoneId(int deletedZoneId) {
        List<Location> zones = tableService.getLocations();
        if (zones.isEmpty()) {
            return -1;
        }

        for (Location zone : zones) {
            if (zone.getId() != deletedZoneId) {
                return zone.getId();
            }
        }

        return zones.get(0).getId();
    }

    private void reloadZonesAndSelect(int zoneId) {
        if (zonesNavbarController != null) {
            zonesNavbarController.loadZones();
            if (zoneId > 0) {
                zonesNavbarController.setActiveZone(zoneId);
            }
        }

        if (zoneId > 0) {
            setActiveZone(zoneId);
        } else {
            activeZoneId = -1;
            renderTables();
        }
    }

    private record TablePosition(BigDecimal posX, BigDecimal posY) {
    }

    private record ZoneOption(int id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}
