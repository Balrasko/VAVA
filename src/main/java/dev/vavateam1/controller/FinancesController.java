package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FinancesController {

    @FXML private Label itemIdLabel;
    @FXML private Label itemNameLabel;
    @FXML private Label soldPiecesLabel;
    @FXML private Label pricePerPieceLabel;
    @FXML private Label dailySalesLabel;
    @FXML private Label soldItemsTotalLabel;

    @FXML
    private void initialize() {
        setDailySalesSummary("2453.65");
        setSoldItemsTotalLabel("45");
        setTopSellingItem("22", "Kuraci rezen", "12", "12.50");
    }

    public void setDailySalesSummary(String dailySales) {
        dailySalesLabel.setText(dailySales);
    }

    public void setSoldItemsTotalLabel(String soldItems) {
        soldItemsTotalLabel.setText(soldItems);
    }

    public void setTopSellingItem(String itemId, String itemName, String soldPieces, String pricePerPiece) {
        itemIdLabel.setText(itemId);
        itemNameLabel.setText(itemName);
        soldPiecesLabel.setText(soldPieces);
        pricePerPieceLabel.setText(pricePerPiece);
    }

    @FXML
    private void onExportFinanceReport() {
        // Placeholder for future backend integration
    }
}
