package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class ClosingController {

    @FXML
    private Button closingButton;

    @FXML
    private Button printButton;

    @FXML
    private Button cashFloatButton;

    @FXML
    private Button withdrawalButton;

    @FXML
    private Label totalPaidLabel;

    @FXML
    private Label totalTipsLabel;

    @FXML
    private Label grandTotalLabel;

    @FXML
    private Label cashFloatValueLabel;

    @FXML
    private Label cashValueLabel;

    @FXML
    private Label cardValueLabel;


    @FXML
    public void initialize() {
        // Placeholder initial values.
        setClosingSummary(
            "420.69",
            "32.40",
            "306.64",
            "100",
            "6.64",
            "300.00"
        );
    }

    public void setClosingSummary(
            String totalPaid,
            String totalTips,
            String grandTotal,
            String cashFloat,
            String cash,
            String card
    ) {
        totalPaidLabel.setText(totalPaid);
        totalTipsLabel.setText(totalTips);
        grandTotalLabel.setText(grandTotal);
        cashFloatValueLabel.setText(cashFloat);
        cashValueLabel.setText(cash);
        cardValueLabel.setText(card);
    }
        

    @FXML
    private void onClosing() {
        // TODO 
    }

    @FXML
    private void onPrint() {
        // TODO 
    }

    @FXML
    private void onCashFloat() {
        // TODO 
    }

    @FXML
    private void onWithdrawal() {
        // TODO 
    }
}
