package dev.vavateam1.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.inject.Inject;

import dev.vavateam1.dao.FinanceDao;
import dev.vavateam1.report.FinanceItemReport;
import dev.vavateam1.report.FinanceReport;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FinancesController {

    private static final String HEADER_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d; -fx-cursor:hand;";
    private static final String ROW_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d;";
    private static final String ROW_STYLE = "-fx-background-color:#f4f4f4; -fx-background-radius:16; -fx-padding:10 16 10 16;";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    @FXML private Label itemIdHeaderLabel;
    @FXML private Label itemNameHeaderLabel;
    @FXML private Label soldPiecesHeaderLabel;
    @FXML private Label pricePerPieceHeaderLabel;
    @FXML private VBox itemsContainer;
    @FXML private Label dailySalesLabel;
    @FXML private Label soldItemsTotalLabel;
    @FXML private TextField searchField;

    private final FinanceDao financeDao;
    private final List<FinanceItemReport> financeItems = new ArrayList<>();

    private SortField activeSortField = SortField.ITEM_ID;
    private boolean ascendingSort = true;
    private FinanceReport currentReport;
    private Pattern activeSearchPattern;

    @Inject
    public FinancesController(FinanceDao financeDao) {
        this.financeDao = financeDao;
    }

    @FXML
    private void initialize() {
        searchField.setOnAction(event -> onApplyFilter());
        reloadReport();
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
    private void onSortBySoldPieces() {
        toggleSort(SortField.SOLD_PIECES);
    }

    @FXML
    private void onSortByPricePerPiece() {
        toggleSort(SortField.PRICE_PER_PIECE);
    }

    @FXML
    private void onApplyFilter() {
        activeSearchPattern = compilePattern(searchField.getText());
        renderItems();
    }

    @FXML
    private void onExportFinanceReport() {
        if (currentReport == null) {
            showError("Finance report export failed", "There is no finance report loaded.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export finance report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fileChooser.setInitialFileName("finance-report-" + currentReport.reportDate().format(FILE_DATE_FORMAT) + ".csv");

        Window window = itemsContainer.getScene() != null ? itemsContainer.getScene().getWindow() : null;
        var selectedFile = fileChooser.showSaveDialog(window);
        if (selectedFile == null) {
            return;
        }

        List<FinanceItemReport> itemsToExport = getFilteredItems();

        try (BufferedWriter writer = Files.newBufferedWriter(selectedFile.toPath())) {
            writer.write("report_date,daily_sales,sold_items_total");
            writer.newLine();
            writer.write(String.format("%s,%s,%d",
                    currentReport.reportDate(),
                    formatDecimal(currentReport.dailySales()),
                    currentReport.soldItemsTotal()));
            writer.newLine();
            writer.newLine();
            writer.write("item_id,name,sold_pieces,price_per_piece");
            writer.newLine();

            for (FinanceItemReport item : itemsToExport) {
                writer.write(String.format("%d,%s,%d,%s",
                        item.itemId(),
                        escapeCsv(item.name()),
                        item.soldPieces(),
                        formatDecimal(item.pricePerPiece())));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export finance report", e);
        }
    }

    private void reloadReport() {
        currentReport = financeDao.getFinanceReport();
        financeItems.clear();
        financeItems.addAll(currentReport.items());
        sortItems();
        refreshSortHeaderLabels();
        dailySalesLabel.setText(formatDecimal(currentReport.dailySales()));
        soldItemsTotalLabel.setText(String.valueOf(currentReport.soldItemsTotal()));
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
        Comparator<FinanceItemReport> comparator = switch (activeSortField) {
            case ITEM_ID -> Comparator.comparingInt(FinanceItemReport::itemId);
            case NAME -> Comparator.comparing(item -> item.name().toLowerCase());
            case SOLD_PIECES -> Comparator.comparingInt(FinanceItemReport::soldPieces);
            case PRICE_PER_PIECE -> Comparator.comparing(FinanceItemReport::pricePerPiece);
        };

        if (!ascendingSort) {
            comparator = comparator.reversed();
        }

        financeItems.sort(comparator);
    }

    private void refreshSortHeaderLabels() {
        itemIdHeaderLabel.setText(buildHeaderText("Item ID", SortField.ITEM_ID));
        itemNameHeaderLabel.setText(buildHeaderText("Name", SortField.NAME));
        soldPiecesHeaderLabel.setText(buildHeaderText("Sold pieces", SortField.SOLD_PIECES));
        pricePerPieceHeaderLabel.setText(buildHeaderText("Price per piece", SortField.PRICE_PER_PIECE));

        itemIdHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        itemNameHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        soldPiecesHeaderLabel.setStyle(HEADER_LABEL_STYLE);
        pricePerPieceHeaderLabel.setStyle(HEADER_LABEL_STYLE);
    }

    private String buildHeaderText(String baseText, SortField field) {
        if (field != activeSortField) {
            return baseText;
        }

        return baseText + (ascendingSort ? " ↑" : " ↓");
    }

    private void renderItems() {
        itemsContainer.getChildren().clear();
        for (FinanceItemReport item : getFilteredItems()) {
            itemsContainer.getChildren().add(createItemRow(item));
        }
    }

    private List<FinanceItemReport> getFilteredItems() {
        if (activeSearchPattern == null) {
            return new ArrayList<>(financeItems);
        }

        return financeItems.stream()
                .filter(this::matchesSearch)
                .toList();
    }

    private boolean matchesSearch(FinanceItemReport item) {
        String searchable = item.itemId() + " " + item.name() + " " + item.soldPieces() + " " + formatDecimal(item.pricePerPiece());
        return activeSearchPattern.matcher(searchable).find();
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

    private GridPane createItemRow(FinanceItemReport item) {
        GridPane row = new GridPane();
        row.setHgap(0);
        row.setStyle(ROW_STYLE);

        row.getColumnConstraints().addAll(
                createColumnConstraint(),
                createColumnConstraint(),
                createColumnConstraint(),
                createColumnConstraint());

        row.add(createRowLabel(String.valueOf(item.itemId())), 0, 0);
        row.add(createRowLabel(item.name()), 1, 0);
        row.add(createRowLabel(String.valueOf(item.soldPieces())), 2, 0);
        row.add(createRowLabel(formatDecimal(item.pricePerPiece())), 3, 0);

        return row;
    }

    private ColumnConstraints createColumnConstraint() {
        ColumnConstraints constraint = new ColumnConstraints();
        constraint.setPercentWidth(25);
        constraint.setHalignment(HPos.CENTER);
        return constraint;
    }

    private Label createRowLabel(String text) {
        Label label = new Label(text);
        label.setStyle(ROW_LABEL_STYLE);
        return label;
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString();
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

    private enum SortField {
        ITEM_ID,
        NAME,
        SOLD_PIECES,
        PRICE_PER_PIECE
    }
}
