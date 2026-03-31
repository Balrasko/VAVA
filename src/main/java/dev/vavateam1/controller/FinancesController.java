package dev.vavateam1.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FinancesController {

    private static final String HEADER_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d; -fx-cursor:hand;";
    private static final String ROW_LABEL_STYLE = "-fx-font-size:14; -fx-text-fill:#1d1d1d;";
    private static final String ROW_STYLE = "-fx-background-color:#f4f4f4; -fx-background-radius:16; -fx-padding:10 16 10 16;";

    @FXML private Label itemIdHeaderLabel;
    @FXML private Label itemNameHeaderLabel;
    @FXML private Label soldPiecesHeaderLabel;
    @FXML private Label pricePerPieceHeaderLabel;
    @FXML private VBox itemsContainer;
    @FXML private Label dailySalesLabel;
    @FXML private Label soldItemsTotalLabel;

    private final List<FinanceItem> financeItems = new ArrayList<>();
    private SortField activeSortField = SortField.ITEM_ID;
    private boolean ascendingSort = true;

    @FXML
    private void initialize() {
        setDailySalesSummary("2453.65");
        setSoldItemsTotalLabel("45");
        initializeSampleItems();
        sortItems();
        refreshSortHeaderLabels();
        renderItems();
    }

    public void setDailySalesSummary(String dailySales) {
        dailySalesLabel.setText(dailySales);
    }

    public void setSoldItemsTotalLabel(String soldItems) {
        soldItemsTotalLabel.setText(soldItems);
    }

    public void setTopSellingItem(String itemId, String itemName, String soldPieces, String pricePerPiece) {
        if (financeItems.isEmpty()) {
            financeItems.add(new FinanceItem(
                    Integer.parseInt(itemId),
                    itemName,
                    Integer.parseInt(soldPieces),
                    Double.parseDouble(pricePerPiece)));
        } else {
            financeItems.set(0, new FinanceItem(
                    Integer.parseInt(itemId),
                    itemName,
                    Integer.parseInt(soldPieces),
                    Double.parseDouble(pricePerPiece)));
        }

        sortItems();
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
    private void onSortBySoldPieces() {
        toggleSort(SortField.SOLD_PIECES);
    }

    @FXML
    private void onSortByPricePerPiece() {
        toggleSort(SortField.PRICE_PER_PIECE);
    }

    @FXML
    private void onExportFinanceReport() {
        // Placeholder for future backend integration
    }

    private void initializeSampleItems() {
        financeItems.clear();
        financeItems.add(new FinanceItem(22, "Kuraci rezen", 12, 12.50));
        financeItems.add(new FinanceItem(14, "Hranolky", 18, 4.20));
        financeItems.add(new FinanceItem(31, "Cesnakova polievka", 9, 5.90));
        financeItems.add(new FinanceItem(8, "Limonada", 6, 3.80));
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
        Comparator<FinanceItem> comparator = switch (activeSortField) {
            case ITEM_ID -> Comparator.comparingInt(FinanceItem::itemId);
            case NAME -> Comparator.comparing(item -> item.name().toLowerCase());
            case SOLD_PIECES -> Comparator.comparingInt(FinanceItem::soldPieces);
            case PRICE_PER_PIECE -> Comparator.comparingDouble(FinanceItem::pricePerPiece);
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

        for (FinanceItem item : financeItems) {
            itemsContainer.getChildren().add(createItemRow(item));
        }
    }

    private GridPane createItemRow(FinanceItem item) {
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
        row.add(createRowLabel(String.format("%.2f", item.pricePerPiece())), 3, 0);

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

    private enum SortField {
        ITEM_ID,
        NAME,
        SOLD_PIECES,
        PRICE_PER_PIECE
    }

    private record FinanceItem(int itemId, String name, int soldPieces, double pricePerPiece) {
    }
}
