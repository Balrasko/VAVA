package dev.vavateam1.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.google.inject.Inject;

import dev.vavateam1.model.ClosingSummary;
import dev.vavateam1.model.User;
import dev.vavateam1.service.AuthService;
import dev.vavateam1.service.ClosingService;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ClosingController {

    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final ClosingService closingService;
    private final AuthService authService;

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
    private HBox rootContainer;

    private ClosingSummary currentSummary;

    @Inject
    public ClosingController(ClosingService closingService, AuthService authService) {
        this.closingService = closingService;
        this.authService = authService;
    }

    @FXML
    public void initialize() {
        reloadSummary();
    }

    public void setClosingSummary(ClosingSummary summary) {
        currentSummary = summary;
        totalPaidLabel.setText(formatMoney(summary.totalPaid()));
        totalTipsLabel.setText(formatMoney(summary.totalTips()));
        grandTotalLabel.setText(formatMoney(summary.grandTotal()));
        cashFloatValueLabel.setText(formatMoney(summary.cashFloat()));
        cashValueLabel.setText(formatMoney(summary.cash()));
        cardValueLabel.setText(formatMoney(summary.card()));
    }

    @FXML
    private void onClosing() {
        User currentUser = requireCurrentUser();
        if (currentUser == null) {
            return;
        }

        boolean created = closingService.closeDay(currentUser.getId());
        if (created) {
            showInfo(
                    "Closing created",
                    "Closing for " + currentSummary.businessDate().format(REPORT_DATE_FORMAT) + " was saved.");
            return;
        }

        showWarning(
                "Closing already exists",
                "Closing for " + currentSummary.businessDate().format(REPORT_DATE_FORMAT) + " has already been saved.");
    }

    @FXML
    private void onPrint() {
        if (currentSummary == null) {
            showWarning("Nothing to print", "Closing summary is not loaded.");
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.showPrintDialog(resolveWindow())) {
            boolean success = printerJob.printPage(rootContainer);
            if (success) {
                printerJob.endJob();
                return;
            }
        }

        exportClosingReport();
    }

    @FXML
    private void onCashFloat() {
        handleAmountAction("Insert cash float", (userId, amount) -> closingService.addCashFloat(userId, amount));
    }

    @FXML
    private void onWithdrawal() {
        handleAmountAction("Withdraw cash", (userId, amount) -> closingService.withdrawCash(userId, amount));
    }

    private void reloadSummary() {
        setClosingSummary(closingService.getClosingSummary());
    }

    private void handleAmountAction(String title, AmountAction action) {
        User currentUser = requireCurrentUser();
        if (currentUser == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(result.get().trim()).setScale(2, RoundingMode.HALF_UP);
            currentSummary = action.execute(currentUser.getId(), amount);
            setClosingSummary(currentSummary);
        } catch (NumberFormatException e) {
            showError("Invalid amount", "Please enter a valid numeric amount.");
        } catch (IllegalArgumentException e) {
            showError("Invalid amount", e.getMessage());
        }
    }

    private void exportClosingReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save closing report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        fileChooser.setInitialFileName("closing-" + currentSummary.businessDate().format(REPORT_DATE_FORMAT) + ".txt");

        var selectedFile = fileChooser.showSaveDialog(resolveWindow());
        if (selectedFile == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(selectedFile.toPath())) {
            writer.write(buildPrintableReport());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export closing report", e);
        }
    }

    private String buildPrintableReport() {
        return """
                Closing report
                Date: %s

                Total paid: %s
                Total tips: %s
                Grand total: %s
                Cash float: %s
                Cash: %s
                Card: %s
                """.formatted(
                currentSummary.businessDate().format(REPORT_DATE_FORMAT),
                formatMoney(currentSummary.totalPaid()),
                formatMoney(currentSummary.totalTips()),
                formatMoney(currentSummary.grandTotal()),
                formatMoney(currentSummary.cashFloat()),
                formatMoney(currentSummary.cash()),
                formatMoney(currentSummary.card()));
    }

    private User requireCurrentUser() {
        User currentUser = authService.getUser();
        if (currentUser == null) {
            showError("User session missing", "No logged-in user was found.");
        }
        return currentUser;
    }

    private String formatMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Window resolveWindow() {
        return rootContainer != null && rootContainer.getScene() != null ? rootContainer.getScene().getWindow() : null;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface AmountAction {
        ClosingSummary execute(int userId, BigDecimal amount);
    }
}
