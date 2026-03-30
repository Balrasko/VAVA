package dev.vavateam1.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class InventoryController {

    private static final String HEADER_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d; -fx-font-weight:bold; -fx-cursor:hand;";
    private static final String ROW_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d;";
    private static final String ROW_STYLE = "white; -fx-background-radius:16; -fx-padding:10 16 10 16;";
    private static final String STATUS_STYLE_CRITICAL = "-fx-text-fill:#ef4444; -fx-font-size:20;";
    private static final String STATUS_STYLE_LOW = "-fx-text-fill:#eab308; -fx-font-size:20;";
    private static final String STATUS_STYLE_OK = "-fx-text-fill:#22c55e; -fx-font-size:20;";

    @FXML
    private Button allItemsButton;
    @FXML
    private Button lowStockButton;
    @FXML
    private Button criticalButton;
    @FXML
    private Label itemIdHeaderLabel;
    @FXML
    private Label nameHeaderLabel;
    @FXML
    private Label quantityHeaderLabel;
    @FXML
    private Label minimalQuantityHeaderLabel;
    @FXML
    private Label statusHeaderLabel;
    @FXML
    private VBox itemsContainer;

    private final List<InventoryItem> allItems = new ArrayList<>();
    private CurrentFilter currentFilter = CurrentFilter.ALL;
    private SortField activeSortField = SortField.ITEM_ID;
    private boolean ascendingSort = true;

    @FXML
    private void initialize() {
        initializeSampleItems();
        sortItems();
        refreshSortHeaderLabels();
        renderItems();
    }

    @FXML
    private void onFilterAllItems() {
        currentFilter = CurrentFilter.ALL;
        updateButtonStyles();
        renderItems();
    }

    @FXML
    private void onFilterLowStock() {
        currentFilter = CurrentFilter.LOW_STOCK;
        updateButtonStyles();
        renderItems();
    }

    @FXML
    private void onFilterCritical() {
        currentFilter = CurrentFilter.CRITICAL;
        updateButtonStyles();
        renderItems();
    }

    @FXML
    private void onSortByItemId() {
        toggleSort(SortField.ITEM_ID);
    }

    @FXML
    private void onSortByName() {
        toggleSort(SortField.NAME);
    }

    @FXML
    private void onSortByQuantity() {
        toggleSort(SortField.QUANTITY);
    }

    @FXML
    private void onSortByMinimalQuantity() {
        toggleSort(SortField.MINIMAL_QUANTITY);
    }

    @FXML
    private void onSortByStatus() {
        toggleSort(SortField.STATUS);
    }

    private void initializeSampleItems() {
        allItems.clear();
        allItems.add(new InventoryItem(1, "Chicken Breast", 45, 50, ItemStatus.OK));
        allItems.add(new InventoryItem(2, "Beef Steak", 12, 30, ItemStatus.LOW));
        allItems.add(new InventoryItem(3, "Tomato Sauce", 3, 20, ItemStatus.CRITICAL));
        allItems.add(new InventoryItem(4, "Olive Oil", 8, 10, ItemStatus.LOW));
        allItems.add(new InventoryItem(5, "Pasta", 120, 50, ItemStatus.OK));
        allItems.add(new InventoryItem(6, "Flour", 2, 25, ItemStatus.CRITICAL));
        allItems.add(new InventoryItem(7, "Eggs", 30, 40, ItemStatus.LOW));
        allItems.add(new InventoryItem(8, "Salt", 200, 100, ItemStatus.OK));
    }

    private void toggleSort(SortField selectedField) {
        if (activeSortField == selectedField) {
            ascendingSort = !ascendingSort;
        } else {
            activeSortField = selectedField;
            ascendingSort = true;
        }

        sortItems();
        refreshSortHeaderLabels();
        renderItems();
    }

    private void sortItems() {
        Comparator<InventoryItem> comparator = switch (activeSortField) {
            case ITEM_ID -> Comparator.comparingInt(InventoryItem::itemId);
            case NAME -> Comparator.comparing(item -> item.name().toLowerCase());
            case QUANTITY -> Comparator.comparingInt(InventoryItem::quantity);
            case MINIMAL_QUANTITY -> Comparator.comparingInt(InventoryItem::minimalQuantity);
            case STATUS -> Comparator.comparing(item -> item.status().name());
        };

        if (!ascendingSort) {
            comparator = comparator.reversed();
        }

        allItems.sort(comparator);
    }

    private void refreshSortHeaderLabels() {
        itemIdHeaderLabel.setText(buildHeaderText("Item ID", SortField.ITEM_ID));
        nameHeaderLabel.setText(buildHeaderText("Name", SortField.NAME));
        quantityHeaderLabel.setText(buildHeaderText("Quantity", SortField.QUANTITY));
        minimalQuantityHeaderLabel.setText(buildHeaderText("Minimal quantity", SortField.MINIMAL_QUANTITY));
        statusHeaderLabel.setText(buildHeaderText("Status", SortField.STATUS));

        itemIdHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        nameHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        quantityHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        minimalQuantityHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        statusHeaderLabel.setStyle(HEADER_LABEL_STYLE);
    }

    private String buildHeaderText(String baseText, SortField field) {
        if (field != activeSortField) {
            return baseText;
        }

        return baseText + (ascendingSort ? " ↑" : " ↓");
    }

    private void renderItems() {
        itemsContainer.getChildren().clear();

        List<InventoryItem> itemsToDisplay = filterItems();

        for (InventoryItem item : itemsToDisplay) {
            itemsContainer.getChildren().add(createItemRow(item));
        }
    }

    private List<InventoryItem> filterItems() {
        return switch (currentFilter) {
            case ALL -> new ArrayList<>(allItems);
            case LOW_STOCK -> allItems.stream()
                    .filter(item -> item.status() == ItemStatus.LOW || item.status() == ItemStatus.CRITICAL)
                    .toList();
            case CRITICAL -> allItems.stream()
                    .filter(item -> item.status() == ItemStatus.CRITICAL)
                    .toList();
        };
    }

    private void updateButtonStyles() {
        String activeStyle = "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:8 20 8 20; -fx-cursor:hand; -fx-font-size:14;";
        String inactiveStyle = "-fx-background-color:#9ecaff; -fx-text-fill:#1d1d1d; -fx-background-radius:20; -fx-padding:8 20 8 20; -fx-cursor:hand; -fx-font-size:14;";

        allItemsButton.setStyle(currentFilter == CurrentFilter.ALL ? activeStyle : inactiveStyle);
        lowStockButton.setStyle(currentFilter == CurrentFilter.LOW_STOCK ? activeStyle : inactiveStyle);
        criticalButton.setStyle(currentFilter == CurrentFilter.CRITICAL ? activeStyle : inactiveStyle);
    }

    private GridPane createItemRow(InventoryItem item) {
        GridPane row = new GridPane();
        row.setHgap(0);
        row.setStyle("-fx-background-color:" + ROW_STYLE);

        row.getColumnConstraints().addAll(
                createColumnConstraint(20),
                createColumnConstraint(25),
                createColumnConstraint(15),
                createColumnConstraint(20),
                createColumnConstraint(20));

        row.add(createRowLabel(String.valueOf(item.itemId())), 0, 0);
        row.add(createRowLabel(item.name()), 1, 0);
        row.add(createRowLabel(String.valueOf(item.quantity())), 2, 0);
        row.add(createRowLabel(String.valueOf(item.minimalQuantity())), 3, 0);
        row.add(createStatusIndicator(item.status()), 4, 0);

        return row;
    }

    private ColumnConstraints createColumnConstraint(double percentWidth) {
        ColumnConstraints constraint = new ColumnConstraints();
        constraint.setPercentWidth(percentWidth);
        constraint.setHalignment(HPos.CENTER);
        return constraint;
    }

    private Label createRowLabel(String text) {
        Label label = new Label(text);
        label.setStyle(ROW_LABEL_STYLE);
        return label;
    }

    private Label createStatusIndicator(ItemStatus status) {
        String statusSymbol = switch (status) {
            case CRITICAL -> "●";
            case LOW -> "●";
            case OK -> "●";
        };

        String statusStyle = switch (status) {
            case CRITICAL -> STATUS_STYLE_CRITICAL;
            case LOW -> STATUS_STYLE_LOW;
            case OK -> STATUS_STYLE_OK;
        };

        Label statusLabel = new Label(statusSymbol);
        statusLabel.setStyle(statusStyle);
        return statusLabel;
    }

    private enum ItemStatus {
        OK,
        LOW,
        CRITICAL
    }

    private enum SortField {
        ITEM_ID,
        NAME,
        QUANTITY,
        MINIMAL_QUANTITY,
        STATUS
    }

    private enum CurrentFilter {
        ALL,
        LOW_STOCK,
        CRITICAL
    }

    private record InventoryItem(int itemId, String name, int quantity, int minimalQuantity, ItemStatus status) {
    }
}
