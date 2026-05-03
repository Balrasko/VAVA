package dev.vavateam1.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.inject.Inject;

import dev.vavateam1.dto.FinanceSummary;
import dev.vavateam1.model.Category;
import dev.vavateam1.report.FinanceItemReport;
import dev.vavateam1.report.FinanceReport;
import dev.vavateam1.service.FinanceService;
import dev.vavateam1.util.I18n;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FinancesController {

    private static final String HEADER_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:-app-text; -fx-cursor:hand;";
    private static final String ROW_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:-app-text;";
    private static final String ROW_STYLE = "-fx-background-color:-app-foreground; -fx-background-radius:16; -fx-padding:10 16 10 16;";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    @FXML
    private Label itemIdHeaderLabel;
    @FXML
    private Label itemNameHeaderLabel;
    @FXML
    private Label soldPiecesHeaderLabel;
    @FXML
    private Label pricePerPieceHeaderLabel;
    @FXML
    private VBox itemsContainer;
    @FXML
    private VBox filterPanel;
    @FXML
    private Label dailySalesLabel;
    @FXML
    private Label soldItemsTotalLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<CategoryFilterOption> categoryCombo;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;

    private final FinanceService financeService;
    private final List<FinanceItemReport> financeItems = new ArrayList<>();

    private SortField activeSortField = SortField.ITEM_ID;
    private boolean ascendingSort = true;
    private FinanceReport currentReport;
    private LocalDate latestReportDate;
    private Pattern activeSearchPattern;
    private BigDecimal displayedDailySales = BigDecimal.ZERO;
    private int displayedSoldItemsTotal = 0;

    @Inject
    public FinancesController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @FXML
    private void initialize() {
        searchField.setOnAction(event -> onApplyFilter());
        populateCategoryCombo();
        latestReportDate = financeService.getLatestReportDate();
        loadFinanceItems(null, null, null);
    }

    @FXML
    private void onToggleFilterPanel() {
        boolean showing = filterPanel.isVisible();
        filterPanel.setVisible(!showing);
        filterPanel.setManaged(!showing);
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
        if (!hasValidDateRange()) {
            return;
        }

        LocalDate fromDate = fromDatePicker != null ? fromDatePicker.getValue() : null;
        LocalDate toDate = toDatePicker != null ? toDatePicker.getValue() : null;
        Integer categoryId = getSelectedCategoryId();
        loadFinanceItems(fromDate, toDate, categoryId);
    }

    @FXML
    private void onClearFilter() {
        searchField.clear();
        if (categoryCombo != null)
            categoryCombo.getSelectionModel().selectFirst();
        if (fromDatePicker != null)
            fromDatePicker.setValue(null);
        if (toDatePicker != null)
            toDatePicker.setValue(null);
        activeSearchPattern = null;
        loadFinanceItems(null, null, null);
    }

    @FXML
    private void onExportFinanceReport() {
        if (currentReport == null) {
            showError(I18n.t("finance.exportFailed"), I18n.t("finance.noReportLoaded"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.t("finance.exportTitle"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.t("file.type.xml"), "*.xml"));
        fileChooser.setInitialFileName("finance-report-" + currentReport.reportDate().format(FILE_DATE_FORMAT) + ".xml");

        Window window = itemsContainer.getScene() != null ? itemsContainer.getScene().getWindow() : null;
        var selectedFile = fileChooser.showSaveDialog(window);
        if (selectedFile == null) {
            return;
        }

        try {
            financeService.exportReport(currentReport, selectedFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export finance report", e);
        }
    }

    private void loadFinanceItems(LocalDate fromDate, LocalDate toDate, Integer categoryId) {
        financeItems.clear();
        financeItems.addAll(financeService.getFinanceItems(fromDate, toDate, categoryId));
        sortItems();
        refreshSortHeaderLabels();
        renderItems();
        currentReport = new FinanceReport(
                financeService.resolveReportDate(fromDate, toDate, latestReportDate),
                displayedDailySales,
                displayedSoldItemsTotal,
                getFilteredItems());
    }

    private void populateCategoryCombo() {
        if (categoryCombo == null) {
            return;
        }

        List<CategoryFilterOption> options = new ArrayList<>();
        options.add(CategoryFilterOption.all());

        for (Category category : financeService.getCategories()) {
            options.add(new CategoryFilterOption(category.getId(), category.getName()));
        }

        categoryCombo.getItems().setAll(options);
        categoryCombo.getSelectionModel().selectFirst();
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
        itemIdHeaderLabel.setText(buildHeaderText(I18n.t("finance.itemId"), SortField.ITEM_ID));
        itemNameHeaderLabel.setText(buildHeaderText(I18n.t("common.name"), SortField.NAME));
        soldPiecesHeaderLabel.setText(buildHeaderText(I18n.t("finance.soldPieces"), SortField.SOLD_PIECES));
        pricePerPieceHeaderLabel.setText(buildHeaderText(I18n.t("finance.pricePerPiece"), SortField.PRICE_PER_PIECE));

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
        List<FinanceItemReport> filteredItems = getFilteredItems();
        itemsContainer.getChildren().clear();
        for (FinanceItemReport item : filteredItems) {
            itemsContainer.getChildren().add(createItemRow(item));
        }
        refreshSummary(filteredItems);
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
        String searchable = item.itemId() + " " + item.name() + " " + item.soldPieces() + " "
                + formatDecimal(item.pricePerPiece());
        return activeSearchPattern.matcher(searchable).find();
    }

    private Pattern compilePattern(String rawPattern) {
        if (rawPattern == null || rawPattern.isBlank()) {
            return null;
        }

        try {
            return Pattern.compile(rawPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        } catch (PatternSyntaxException e) {
            showError(I18n.t("finance.invalidRegex"), e.getDescription());
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

    private void refreshSummary(List<FinanceItemReport> filteredItems) {
        FinanceSummary summary = financeService.computeSummary(filteredItems);
        displayedDailySales = summary.totalSales();
        displayedSoldItemsTotal = summary.soldItemsTotal();
        dailySalesLabel.setText(formatDecimal(displayedDailySales));
        soldItemsTotalLabel.setText(String.valueOf(displayedSoldItemsTotal));
    }

    private boolean hasValidDateRange() {
        if (fromDatePicker == null || toDatePicker == null) {
            return true;
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showError(I18n.t("finance.invalidDateRange"), I18n.t("finance.invalidDateRangeMessage"));
            return false;
        }

        return true;
    }

    private Integer getSelectedCategoryId() {
        if (categoryCombo == null) {
            return null;
        }

        CategoryFilterOption selectedOption = categoryCombo.getValue();
        return selectedOption != null ? selectedOption.id() : null;
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

    private record CategoryFilterOption(Integer id, String label) {
        private static CategoryFilterOption all() {
            return new CategoryFilterOption(null, I18n.t("finance.allCategories"));
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
