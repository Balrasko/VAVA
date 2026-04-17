package dev.vavateam1.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.inject.Inject;

import dev.vavateam1.dao.InventoryIngredientDao;
import dev.vavateam1.model.InventoryIngredient;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

public class InventoryController {

    private static final String HEADER_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d; -fx-font-weight:bold; -fx-cursor:hand;";
    private static final String ROW_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d;";
    private static final String STATUS_STYLE_CRITICAL = "-fx-text-fill:#ef4444; -fx-font-size:20;";
    private static final String STATUS_STYLE_LOW = "-fx-text-fill:#eab308; -fx-font-size:20;";
    private static final String STATUS_STYLE_OK = "-fx-text-fill:#22c55e; -fx-font-size:20;";
    private static final List<String> DEFAULT_COLUMN_ORDER = List.of(
            "id", "name", "quantity", "minimal_quantity", "unit", "cost_per_unit", "status");

    @FXML
    private StackPane rootStack;
    @FXML
    private VBox root;
    @FXML
    private Button allItemsButton;
    @FXML
    private Button lowStockButton;
    @FXML
    private Button criticalButton;
    @FXML
    private Button editButton;
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
    private TextField searchField;
    @FXML
    private VBox itemsContainer;
    @FXML
    private Label toastLabel;

    private final InventoryIngredientDao inventoryIngredientDao;
    private final List<InventoryIngredient> allItems = new ArrayList<>();

    private CurrentFilter currentFilter = CurrentFilter.ALL;
    private SortField activeSortField = SortField.ITEM_ID;
    private boolean ascendingSort = true;
    private Pattern activeSearchPattern;

    private boolean editMode = false;

    @Inject
    public InventoryController(InventoryIngredientDao inventoryIngredientDao) {
        this.inventoryIngredientDao = inventoryIngredientDao;
    }

    @FXML
    private void initialize() {
        searchField.setOnAction(event -> onApplyFilter());
        updateButtonStyles();
        reloadInventory();
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

    @FXML
    private void onApplyFilter() {
        activeSearchPattern = compilePattern(searchField.getText());
        renderItems();
    }

    @FXML
    private void onImportInventory() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import inventory");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));

        Window window = itemsContainer.getScene() != null ? itemsContainer.getScene().getWindow() : null;
        var selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile == null) {
            return;
        }

        try {
            List<InventoryIngredient> importedIngredients = readInventoryCsv(selectedFile.toPath());
            inventoryIngredientDao.saveAll(importedIngredients);
            reloadInventory();
        } catch (IOException e) {
            throw new RuntimeException("Failed to import inventory", e);
        }
    }

    @FXML
    private void onExportInventory() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export inventory");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fileChooser.setInitialFileName("inventory-export.csv");

        Window window = itemsContainer.getScene() != null ? itemsContainer.getScene().getWindow() : null;
        var selectedFile = fileChooser.showSaveDialog(window);
        if (selectedFile == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(selectedFile.toPath())) {
            writer.write("id,name,quantity,minimal_quantity,unit,cost_per_unit,status");
            writer.newLine();

            for (InventoryIngredient item : getFilteredItems()) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s",
                        item.getId(),
                        escapeCsv(item.getName()),
                        formatDecimal(item.getQuantity()),
                        formatDecimal(item.getMinimalQuantity()),
                        escapeCsv(item.getUnit()),
                        formatDecimal(item.getCostPerUnit()),
                        statusFor(item).name()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export inventory", e);
        }
    }

    @FXML
    private void toggleEditMode() {
        editMode = !editMode;

        editButton.setText(editMode ? "Done" : "Edit");

        if (editMode) {
            root.setStyle("""
                        -fx-padding: 19;
                        -fx-background-color: #f4f4f4;
                        -fx-border-color: #f00;
                        -fx-border-width: 2;
                    """);
        } else {
            root.setStyle("""
                        -fx-padding: 20;
                        -fx-background-color: #f4f4f4;
                        -fx-border-color: transparent;
                    """);
        }

        renderItems();
    }

    private void reloadInventory() {
        allItems.clear();
        allItems.addAll(inventoryIngredientDao.findAll());
        sortItems();
        refreshSortHeaderLabels();
        renderItems();
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
        Comparator<InventoryIngredient> comparator = switch (activeSortField) {
            case ITEM_ID -> Comparator.comparingInt(InventoryIngredient::getId);
            case NAME -> Comparator.comparing(item -> item.getName().toLowerCase());
            case QUANTITY -> Comparator.comparing(InventoryIngredient::getQuantity);
            case MINIMAL_QUANTITY -> Comparator.comparing(InventoryIngredient::getMinimalQuantity);
            case STATUS -> Comparator.comparing(item -> statusFor(item).name());
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

        if (editMode) {
            itemsContainer.getChildren().add(createAddRow());
        }

        for (InventoryIngredient item : getFilteredItems()) {
            itemsContainer.getChildren().add(createItemRow(item));
        }
    }

    private List<InventoryIngredient> getFilteredItems() {
        return allItems.stream()
                .filter(this::matchesCurrentFilter)
                .filter(this::matchesSearch)
                .toList();
    }

    private boolean matchesCurrentFilter(InventoryIngredient item) {
        ItemStatus status = statusFor(item);
        return switch (currentFilter) {
            case ALL -> true;
            case LOW_STOCK -> status == ItemStatus.LOW || status == ItemStatus.CRITICAL;
            case CRITICAL -> status == ItemStatus.CRITICAL;
        };
    }

    private boolean matchesSearch(InventoryIngredient item) {
        if (activeSearchPattern == null) {
            return true;
        }

        String searchable = item.getId() + " " + item.getName() + " " + formatDecimal(item.getQuantity()) + " "
                + formatDecimal(item.getMinimalQuantity()) + " " + safeValue(item.getUnit()) + " "
                + formatDecimal(item.getCostPerUnit()) + " " + statusFor(item).name();
        return activeSearchPattern.matcher(searchable).find();
    }

    private void updateButtonStyles() {
        String activeStyle = "-fx-background-color: #1e40af; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:8 20 8 20; -fx-cursor:hand; -fx-font-size:14;";
        String inactiveStyle = "-fx-background-color: #9ecaff; -fx-text-fill:#1d1d1d; -fx-background-radius:20; -fx-padding:8 20 8 20; -fx-cursor:hand; -fx-font-size:14;";

        allItemsButton.setStyle(currentFilter == CurrentFilter.ALL ? activeStyle : inactiveStyle);
        lowStockButton.setStyle(currentFilter == CurrentFilter.LOW_STOCK ? activeStyle : inactiveStyle);
        criticalButton.setStyle(currentFilter == CurrentFilter.CRITICAL ? activeStyle : inactiveStyle);
    }

    private GridPane createItemRow(InventoryIngredient item) {
        GridPane row = new GridPane();
        row.setHgap(0);
        row.getStyleClass().add(editMode ? "inv-row-edit" : "inv-row");

        row.getColumnConstraints().addAll(
                createColumnConstraint(18),
                createColumnConstraint(22),
                createColumnConstraint(14),
                createColumnConstraint(18),
                createColumnConstraint(18),
                createColumnConstraint(10));

        row.add(createRowLabel(String.valueOf(item.getId())), 0, 0);
        row.add(createRowLabel(item.getName()), 1, 0);
        row.add(createRowLabel(formatDecimal(item.getQuantity())), 2, 0);
        row.add(createRowLabel(formatDecimal(item.getMinimalQuantity())), 3, 0);
        row.add(createStatusIndicator(statusFor(item)), 4, 0);

        if (editMode) {
            Button deleteButton = new Button("✕");
            deleteButton.setStyle("""
                        -fx-background-color: #ef4444;
                        -fx-text-fill: #fff;
                        -fx-bakcground-radius: 20;
                        -fx-cursor: hand;
                    """);

            deleteButton.setOnAction(e -> confirmDelete(item));

            row.add(deleteButton, 5, 0);
        }

        row.setOnMouseClicked(e -> {
            if (editMode) {
                editItem(item);
            }
        });

        return row;
    }

    private void confirmDelete(InventoryIngredient item) {
        Button yes = new Button("Delete");
        Button no = new Button("Cancel");

        yes.getStyleClass().add("dialog-button");
        yes.setStyle("-fx-background-color: #ef4444");

        no.getStyleClass().add("dialog-button");

        yes.setMinWidth(40);
        no.setMinWidth(40);

        yes.setOnAction(e -> {
            inventoryIngredientDao.delete(item.getId());
            rootStack.getChildren().removeLast();
            reloadInventory();
            showToast("Item deleted.", true);
        });

        no.setOnAction(e -> {
            rootStack.getChildren().removeLast();
        });

        showDialog("Confirm Delete", new Label("Are you sure you want to delete this item?"), List.of(yes, no));
    }

    private void editItem(InventoryIngredient item) {
        TextField nameField = new TextField(item.getName());
        TextField quantityField = new TextField(item.getQuantity().toString());
        TextField minQuantityField = new TextField(item.getMinimalQuantity().toString());

        HBox content = new HBox(10,
                new VBox(20,
                        new Label("Name"),
                        new Label("Quantity"),
                        new Label("Minimum Quantity")),
                new VBox(10,
                        nameField,
                        quantityField,
                        minQuantityField));

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        save.setMinWidth(40);
        cancel.setMinWidth(40);

        save.getStyleClass().add("dialog-button");
        cancel.getStyleClass().add("dialog-button");

        save.setOnAction(e -> {
            try {
                item.setName(nameField.getText());
                item.setQuantity(new BigDecimal(quantityField.getText()));
                item.setMinimalQuantity(new BigDecimal(minQuantityField.getText()));

                inventoryIngredientDao.saveAll(allItems);

                reloadInventory();

                rootStack.getChildren().removeLast();

                showToast("Item edited.", true);
            } catch (Exception ex) {
                showToast("Invalid format in one of the fields.", false);
                return;
            }
        });

        cancel.setOnAction(e -> {
            rootStack.getChildren().removeLast();
        });

        showDialog("Edit Item", content, List.of(save, cancel));
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
        String statusStyle = switch (status) {
            case CRITICAL -> STATUS_STYLE_CRITICAL;
            case LOW -> STATUS_STYLE_LOW;
            case OK -> STATUS_STYLE_OK;
        };

        Label statusLabel = new Label("●");
        statusLabel.setStyle(statusStyle);
        return statusLabel;
    }

    private Label createAddRow() {

        Label row = new Label("Add a new item");
        row.setAlignment(Pos.CENTER);
        row.setStyle("""
                    -fx-text-fill: #000;
                    -fx-font-size: 20px;
                    -fx-font-weight: bold;
                    -fx-background-color: #9ecaff;
                    -fx-background-radius: 16;
                    -fx-border-width: 1;
                    -fx-border-radius: 16;
                    -fx-border-color: #1e88e5;
                    -fx-cursor: hand;
                """);

        row.setMaxWidth(Double.MAX_VALUE);
        row.setMinHeight(50);

        VBox.setVgrow(row, Priority.ALWAYS);

        row.setOnMouseClicked(e -> createNewItem());

        return row;
    }

    private void showDialog(String title, Node content, List<Button> actions) {
        StackPane overlay = new StackPane();
        overlay.setStyle("""
                    -fx-background-color: rgba(0,0,0,0.5);
                """);

        VBox dialog = new VBox(20);
        dialog.setAlignment(Pos.CENTER);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(300);

        dialog.setStyle("""
                    -fx-background-color: #fff;
                    -fx-padding: 20;
                    -fx-background-radius: 16;
                    -fx-border-width: 2;
                    -fx-border-color: #1e88e5;
                    -fx-border-radius: 10;
                """);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        buttonBox.getChildren().addAll(actions);

        dialog.getChildren().addAll(titleLabel, content, buttonBox);
        overlay.getChildren().add(dialog);

        rootStack.getChildren().add(overlay);

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                rootStack.getChildren().remove(overlay);
            }
        });
    }

    private void createNewItem() {
        TextField nameField = new TextField();
        TextField quantityField = new TextField();
        TextField minQuantityField = new TextField();
        TextField costPerUnitField = new TextField();
        TextField unitField = new TextField();

        HBox content = new HBox(10,
                new VBox(20,
                        new Label("Name"),
                        new Label("Quantity"),
                        new Label("Minimum Quantity"),
                        new Label("Cost per unit"),
                        new Label("Unit")),
                new VBox(10,
                        nameField,
                        quantityField,
                        minQuantityField,
                        costPerUnitField,
                        unitField));

        Button save = new Button("Save");
        Button cancel = new Button("Cancel");

        save.setMinWidth(40);
        cancel.setMinWidth(40);

        save.getStyleClass().add("dialog-button");
        cancel.getStyleClass().add("dialog-button");

        save.setOnAction(e -> {
            try {
                InventoryIngredient item = new InventoryIngredient();
                item.setName(nameField.getText());
                item.setQuantity(new BigDecimal(quantityField.getText()));
                item.setMinimalQuantity(new BigDecimal(minQuantityField.getText()));
                item.setCostPerUnit(new BigDecimal(costPerUnitField.getText()));
                item.setUnit(unitField.getText());

                allItems.add(item);
                inventoryIngredientDao.saveAll(allItems);
                reloadInventory();

                rootStack.getChildren().removeLast();

                showToast("New item created.", true);
            } catch (Exception ex) {
                showToast("Invalid format in one of the fields.", false);
                return;
            }
        });

        cancel.setOnAction(e -> {
            rootStack.getChildren().removeLast();
        });

        showDialog("New Item", content, List.of(save, cancel));
    }

    private ItemStatus statusFor(InventoryIngredient item) {
        BigDecimal quantity = zeroIfNull(item.getQuantity());
        BigDecimal minimalQuantity = zeroIfNull(item.getMinimalQuantity());

        if (minimalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return quantity.compareTo(BigDecimal.ZERO) <= 0 ? ItemStatus.CRITICAL : ItemStatus.OK;
        }

        if (quantity.compareTo(minimalQuantity.multiply(new BigDecimal("0.5"))) <= 0) {
            return ItemStatus.CRITICAL;
        }

        if (quantity.compareTo(minimalQuantity) < 0) {
            return ItemStatus.LOW;
        }

        return ItemStatus.OK;
    }

    private void showToast(String message, Boolean msgType) {
        // Display a temporary floating toast style popup

        toastLabel.getStyleClass().add("toast");

        if (!msgType) {
            toastLabel.setStyle("-fx-border-color: #e53b3b;");
        } else {
            toastLabel.setStyle("-fx-border-color: limegreen;");
        }

        toastLabel.setText(message);
        toastLabel.setOpacity(0);
        toastLabel.setVisible(true);

        // Push the label to the front of the StackPane
        toastLabel.toFront();

        // Make the toast fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toastLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Show the toast
        PauseTransition stay = new PauseTransition(Duration.seconds(1.5));

        // Make the toast fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toastLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Hide the label again
        fadeOut.setOnFinished(e -> {
            toastLabel.setVisible(false);
            toastLabel.setOpacity(1);
        });

        // Perform the transition
        new SequentialTransition(fadeIn, stay, fadeOut).play();
    }

    private Pattern compilePattern(String rawPattern) {
        if (rawPattern == null || rawPattern.isBlank()) {
            return null;
        }

        try {
            return Pattern.compile(rawPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            showError("Invalid regex", e.getDescription());
            return activeSearchPattern;
        }
    }

    private List<InventoryIngredient> readInventoryCsv(Path path) throws IOException {
        List<InventoryIngredient> importedItems = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.isBlank()) {
                return importedItems;
            }

            List<String> firstColumns = parseCsvLine(firstLine);
            Map<String, Integer> columnIndex = isHeader(firstColumns)
                    ? buildColumnIndex(firstColumns)
                    : buildColumnIndex(DEFAULT_COLUMN_ORDER);

            if (!isHeader(firstColumns)) {
                importedItems.add(mapImportedIngredient(firstColumns, columnIndex));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                importedItems.add(mapImportedIngredient(parseCsvLine(line), columnIndex));
            }
        }

        return importedItems;
    }

    private boolean isHeader(List<String> columns) {
        return columns.stream().map(this::normalizeColumnName).anyMatch("name"::equals);
    }

    private Map<String, Integer> buildColumnIndex(List<String> columns) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            index.put(normalizeColumnName(columns.get(i)), i);
        }
        return index;
    }

    private InventoryIngredient mapImportedIngredient(List<String> columns, Map<String, Integer> columnIndex) {
        InventoryIngredient ingredient = new InventoryIngredient();
        ingredient.setId(parseInteger(getColumnValue(columns, columnIndex, "id"), 0));
        ingredient.setName(getColumnValue(columns, columnIndex, "name"));
        ingredient.setQuantity(parseDecimal(getColumnValue(columns, columnIndex, "quantity")));
        ingredient.setMinimalQuantity(parseDecimal(getColumnValue(columns, columnIndex, "minimal_quantity")));
        ingredient.setUnit(getColumnValue(columns, columnIndex, "unit"));
        ingredient.setCostPerUnit(parseDecimal(getColumnValue(columns, columnIndex, "cost_per_unit")));
        return ingredient;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char currentChar = line.charAt(i);

            if (currentChar == '"') {
                boolean escapedQuote = inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"';
                if (escapedQuote) {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == ',' && !inQuotes) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }

        values.add(current.toString().trim());
        return values;
    }

    private String getColumnValue(List<String> columns, Map<String, Integer> columnIndex, String columnName) {
        Integer index = columnIndex.get(columnName);
        if (index == null || index >= columns.size()) {
            return "";
        }
        return columns.get(index);
    }

    private String normalizeColumnName(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace(' ', '_');
    }

    private int parseInteger(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatDecimal(BigDecimal value) {
        return zeroIfNull(value).stripTrailingZeros().toPlainString();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
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
}
