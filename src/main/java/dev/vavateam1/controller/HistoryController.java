package dev.vavateam1.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.dto.PaymentDto;
import dev.vavateam1.service.HistoryService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
                    dayLabel = "Today";
                else if (paymentDate.equals(yesterday))
                    dayLabel = "Yesterday";
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
        Label refundedLabel = new Label("REFUNDED");

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

        Label title = new Label("Order summary");
        title.setStyle("-fx-font-size:20; -fx-font-weight:bold;");

        Label orderId = new Label("Order: #" + payment.getId());
        Label orderDate = new Label("Date: " + payment.getCreatedAt().format(DISPLAY_FORMAT));
        Label waiter = new Label("Waiter: #" + payment.getWaiterId());
        Label methodId = new Label("Payment method ID: " + payment.getMethodId());
        Label orderTotal = new Label("Total: " + payment.getAmount().toPlainString() + "€");

        String tipText = payment.getTip() != null
                ? payment.getTip().stripTrailingZeros().toPlainString() + "%"
                : "-";
        Label tip = new Label("Tip: " + tipText);

        String methodText = payment.getPaymentMethodName() != null
                ? payment.getPaymentMethodName()
                : "-";
        Label paymentMethod = new Label("Payment: " + methodText);

        String refundedText = Boolean.TRUE.equals(payment.getRefunded()) ? "Yes" : "No";
        Label refunded = new Label("Refunded: " + refundedText);
        if (Boolean.TRUE.equals(payment.getRefunded())) {
            refunded.setStyle("-fx-text-fill:-app-delete; -fx-font-weight:bold;");
        }

        Label itemsTitle = new Label("Items");
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
            detailContainer.getChildren().add(new Label("No items found."));
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
    private void refundSelectedOrder() {
        if (selectedPayment == null || Boolean.TRUE.equals(selectedPayment.getRefunded())) {
            return;
        }

        try {
            historyService.refund(selectedPayment.getId());
        } catch (IllegalStateException e) {
            showWarning("Order already refunded.");
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

    @FXML
    private void closeDetail() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        selectedPayment = null;
    }
}