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
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class InventoryController {

    private static final String HEADER_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d; -fx-font-weight:bold; -fx-cursor:hand;";
    private static final String ROW_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d;";
    private static final String ROW_STYLE = "white; -fx-background-radius:16; -fx-padding:10 16 10 16;";
    private static final String STATUS_STYLE_CRITICAL = "-fx-text-fill:#ef4444; -fx-font-size:20;";
    private static final String STATUS_STYLE_LOW = "-fx-text-fill:#eab308; -fx-font-size:20;";
    private static final String STATUS_STYLE_OK = "-fx-text-fill:#22c55e; -fx-font-size:20;";
    private static final List<String> DEFAULT_COLUMN_ORDER = List.of(
            "id", "name", "quantity", "minimal_quantity", "unit", "cost_per_unit", "status");

    @FXML private Button allItemsButton;
    @FXML private Button lowStockButton;
    @FXML private Button criticalButton;
    @FXML private Label itemIdHeaderLabel;
    @FXML private Label nameHeaderLabel;
    @FXML private Label quantityHeaderLabel;
    @FXML private Label minimalQuantityHeaderLabel;
    @FXML private Label statusHeaderLabel;
    @FXML private TextField searchField;
    @FXML private VBox itemsContainer;

    private final InventoryIngredientDao inventoryIngredientDao;
    private final List<InventoryIngredient> allItems = new ArrayList<>();

    private CurrentFilter currentFilter = CurrentFilter.ALL;
    private SortField activeSortField = SortField.ITEM_ID;
    private boolean ascendingSort = true;
    private Pattern activeSearchPattern;

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
        String activeStyle = "-fx-background-color:#1e40af; -fx-text-fill:white; -fx-background-radius:20; -fx-padding:8 20 8 20; -fx-cursor:hand; -fx-font-size:14;";
        String inactiveStyle = "-fx-background-color:#9ecaff; -fx-text-fill:#1d1d1d; -fx-background-radius:20; -fx-padding:8 20 8 20; -fx-cursor:hand; -fx-font-size:14;";

        allItemsButton.setStyle(currentFilter == CurrentFilter.ALL ? activeStyle : inactiveStyle);
        lowStockButton.setStyle(currentFilter == CurrentFilter.LOW_STOCK ? activeStyle : inactiveStyle);
        criticalButton.setStyle(currentFilter == CurrentFilter.CRITICAL ? activeStyle : inactiveStyle);
    }

    private GridPane createItemRow(InventoryIngredient item) {
        GridPane row = new GridPane();
        row.setHgap(0);
        row.setStyle("-fx-background-color:" + ROW_STYLE);

        row.getColumnConstraints().addAll(
                createColumnConstraint(20),
                createColumnConstraint(25),
                createColumnConstraint(15),
                createColumnConstraint(20),
                createColumnConstraint(20));

        row.add(createRowLabel(String.valueOf(item.getId())), 0, 0);
        row.add(createRowLabel(item.getName()), 1, 0);
        row.add(createRowLabel(formatDecimal(item.getQuantity())), 2, 0);
        row.add(createRowLabel(formatDecimal(item.getMinimalQuantity())), 3, 0);
        row.add(createStatusIndicator(statusFor(item)), 4, 0);

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
        String statusStyle = switch (status) {
            case CRITICAL -> STATUS_STYLE_CRITICAL;
            case LOW -> STATUS_STYLE_LOW;
            case OK -> STATUS_STYLE_OK;
        };

        Label statusLabel = new Label("●");
        statusLabel.setStyle(statusStyle);
        return statusLabel;
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
