package dev.vavateam1.controller;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.dto.PaymentDto;
import dev.vavateam1.service.HistoryService;
import dev.vavateam1.util.I18n;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class HistoryController {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("d.M.yyyy");

    @FXML
    private VBox ordersContainer;

    @FXML
    private VBox detailPanel;

    @FXML
    private VBox detailContainer;

    @FXML
    private Button refundButton;

    @FXML
    private Button printCopyButton;

    private final HistoryService historyService;
    private PaymentDto selectedPayment;

    @Inject
    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @FXML
    public void initialize() {
        loadPayments();

        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }

    private void loadPayments() {
        ordersContainer.getChildren().clear();
        List<PaymentDto> payments = historyService.getPayments();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastGroupDate = null;

        for (PaymentDto payment : payments) {
            LocalDate paymentDate = payment.getCreatedAt().toLocalDate();

            if (!paymentDate.equals(lastGroupDate)) {
                String dayLabel;
                if (paymentDate.equals(today))
                    dayLabel = I18n.t("history.today");
                else if (paymentDate.equals(yesterday))
                    dayLabel = I18n.t("history.yesterday");
                else
                    dayLabel = paymentDate.format(DAY_FORMAT);

                addDay(dayLabel);
                lastGroupDate = paymentDate;
            }

            addOrder(payment);
        }
    }

    private void addDay(String day) {
        Label label = new Label(day);
        label.setStyle(
                "-fx-background-color:-app-foreground;" +
                        "-fx-padding:8 18;" +
                        "-fx-background-radius:20;" +
                        "-fx-font-weight:bold;");
        ordersContainer.getChildren().add(label);
    }

    private void addOrder(PaymentDto payment) {
        HBox card = new HBox(20);
        card.setStyle(
                "-fx-background-color:-app-foreground;" +
                        "-fx-padding:15 25;" +
                        "-fx-background-radius:25;");

        Label orderId = new Label("#" + payment.getId());
        Label orderDate = new Label(payment.getCreatedAt().format(DISPLAY_FORMAT));
        Label orderTotal = new Label(payment.getAmount().toPlainString() + "€");
        Label refundedLabel = new Label(I18n.t("history.refunded"));

        orderTotal.setStyle("-fx-font-weight:bold;");
        refundedLabel.setStyle("-fx-text-fill:-app-delete; -fx-font-weight:bold;");
        refundedLabel.setVisible(Boolean.TRUE.equals(payment.getRefunded()));
        refundedLabel.setManaged(Boolean.TRUE.equals(payment.getRefunded()));
        HBox.setHgrow(orderDate, Priority.ALWAYS);

        card.getChildren().addAll(orderId, orderDate, refundedLabel, orderTotal);
        card.setOnMouseClicked(e -> showDetail(payment));

        ordersContainer.getChildren().add(card);
    }

    private void showDetail(PaymentDto payment) {
        selectedPayment = payment;
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);
        refundButton.setDisable(Boolean.TRUE.equals(payment.getRefunded()));

        detailContainer.getChildren().clear();
        List<OrderItemDto> orderItems = historyService.getOrderItemsByPaymentId(payment.getId());

        Label title = new Label(I18n.t("history.orderSummary"));
        title.setStyle("-fx-font-size:20; -fx-font-weight:bold;");

        Label orderId = new Label(I18n.t("history.order", String.valueOf(payment.getId())));
        Label orderDate = new Label(I18n.t("history.date", payment.getCreatedAt().format(DISPLAY_FORMAT)));
        Label waiter = new Label(I18n.t("history.waiter", String.valueOf(payment.getWaiterId())));
        Label methodId = new Label(I18n.t("history.paymentMethodId", String.valueOf(payment.getMethodId())));
        Label orderTotal = new Label(I18n.t("history.total", payment.getAmount().toPlainString()));

        String tipText = payment.getTip() != null
                ? payment.getTip().stripTrailingZeros().toPlainString() + "%"
                : "-";
        Label tip = new Label(I18n.t("history.tip", tipText));

        String methodText = payment.getPaymentMethodName() != null
                ? localizePaymentMethod(payment.getPaymentMethodName())
                : "-";
        Label paymentMethod = new Label(I18n.t("history.payment", methodText));

        String refundedText = Boolean.TRUE.equals(payment.getRefunded()) ? I18n.t("common.yes") : I18n.t("common.no");
        Label refunded = new Label(I18n.t("history.refundedValue", refundedText));
        if (Boolean.TRUE.equals(payment.getRefunded())) {
            refunded.setStyle("-fx-text-fill:-app-delete; -fx-font-weight:bold;");
        }

        Label itemsTitle = new Label(I18n.t("history.items"));
        itemsTitle.setStyle("-fx-font-size:16; -fx-font-weight:bold;");

        detailContainer.getChildren().addAll(
                title,
                orderId,
                orderDate,
                waiter,
                methodId,
                orderTotal,
                tip,
                paymentMethod,
                refunded,
                itemsTitle);

        if (orderItems.isEmpty()) {
            detailContainer.getChildren().add(new Label(I18n.t("history.noItems")));
            return;
        }

        for (OrderItemDto orderItem : orderItems) {
            String priceText = orderItem.getPrice() != null
                    ? orderItem.getPrice().stripTrailingZeros().toPlainString() + "€"
                    : "-";
            Label itemLabel = new Label(orderItem.getName() + " x" + orderItem.getQuantity() + " - " + priceText);
            detailContainer.getChildren().add(itemLabel);
        }
    }

    @FXML
    private void printCopy() {
        if (selectedPayment == null) {
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        Window window = detailPanel.getScene() != null ? detailPanel.getScene().getWindow() : null;
        if (printerJob != null && printerJob.showPrintDialog(window)) {
            boolean success = printerJob.printPage(detailPanel);
            if (success) {
                printerJob.endJob();
                return;
            }
        }

        exportOrderReceipt();
    }

    private void exportOrderReceipt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.t("history.saveReceipt"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.t("file.type.text"), "*.txt"));
        fileChooser.setInitialFileName("order-" + selectedPayment.getId() + ".txt");

        Window window = detailPanel.getScene() != null ? detailPanel.getScene().getWindow() : null;
        var selectedFile = fileChooser.showSaveDialog(window);
        if (selectedFile == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(selectedFile.toPath())) {
            writer.write(buildReceiptText());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export order receipt", e);
        }
    }

    private String buildReceiptText() {
        List<OrderItemDto> orderItems = historyService.getOrderItemsByPaymentId(selectedPayment.getId());
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("history.receiptTitle")).append("\n");
        sb.append(I18n.t("history.order", String.valueOf(selectedPayment.getId()))).append("\n");
        sb.append(I18n.t("history.date", selectedPayment.getCreatedAt().format(DISPLAY_FORMAT))).append("\n");
        sb.append(I18n.t("history.waiter", String.valueOf(selectedPayment.getWaiterId()))).append("\n");

        String methodText = selectedPayment.getPaymentMethodName() != null ? localizePaymentMethod(selectedPayment.getPaymentMethodName()) : "-";
        sb.append(I18n.t("history.payment", methodText)).append("\n");

        String tipText = selectedPayment.getTip() != null
                ? selectedPayment.getTip().stripTrailingZeros().toPlainString() + "%"
                : "-";
        sb.append(I18n.t("history.tip", tipText)).append("\n\n");
        sb.append(I18n.t("history.items")).append(":\n");

        for (OrderItemDto item : orderItems) {
            String priceText = item.getPrice() != null
                    ? item.getPrice().stripTrailingZeros().toPlainString() + "€"
                    : "-";
            sb.append("  ").append(item.getName()).append(" x").append(item.getQuantity())
                    .append(" - ").append(priceText).append("\n");
        }

        sb.append("\n").append(I18n.t("history.total", selectedPayment.getAmount().toPlainString())).append("\n");

        if (Boolean.TRUE.equals(selectedPayment.getRefunded())) {
            sb.append(I18n.t("history.refunded")).append("\n");
        }

        return sb.toString();
    }

    @FXML
    private void refundSelectedOrder() {
        if (selectedPayment == null || Boolean.TRUE.equals(selectedPayment.getRefunded())) {
            return;
        }

        try {
            historyService.refund(selectedPayment.getId());
        } catch (IllegalStateException e) {
            showWarning(I18n.t("history.alreadyRefunded"));
        }

        loadPayments();
        PaymentDto refreshedPayment = historyService.getPayments().stream()
                .filter(payment -> payment.getId() == selectedPayment.getId())
                .findFirst()
                .orElse(null);

        if (refreshedPayment != null) {
            showDetail(refreshedPayment);
            return;
        }

        closeDetail();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String localizePaymentMethod(String methodName) {
        return switch (methodName.toLowerCase()) {
            case "cash" -> I18n.t("common.cash");
            case "card" -> I18n.t("common.card");
            case "meal card" -> I18n.t("payment.mealCard");
            default -> methodName;
        };
    }

    @FXML
    private void closeDetail() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        selectedPayment = null;
    }
}
