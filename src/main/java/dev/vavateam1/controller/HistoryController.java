package dev.vavateam1.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dto.PaymentDto;
import dev.vavateam1.service.HistoryService;
import javafx.fxml.FXML;
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

    private final HistoryService historyService;

    @Inject
    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @FXML
    public void initialize() {
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

        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }

    private void addDay(String day) {
        Label label = new Label(day);
        label.setStyle(
                "-fx-background-color:white;" +
                        "-fx-padding:8 18;" +
                        "-fx-background-radius:20;" +
                        "-fx-font-weight:bold;");
        ordersContainer.getChildren().add(label);
    }

    private void addOrder(PaymentDto payment) {
        HBox card = new HBox(20);
        card.setStyle(
                "-fx-background-color:white;" +
                        "-fx-padding:15 25;" +
                        "-fx-background-radius:25;");

        Label orderId = new Label("#" + payment.getId());
        Label orderDate = new Label(payment.getCreatedAt().format(DISPLAY_FORMAT));
        Label orderTotal = new Label(payment.getAmount().toPlainString() + "€");

        orderTotal.setStyle("-fx-font-weight:bold;");
        HBox.setHgrow(orderDate, Priority.ALWAYS);

        card.getChildren().addAll(orderId, orderDate, orderTotal);
        card.setOnMouseClicked(e -> showDetail(payment));

        ordersContainer.getChildren().add(card);
    }

    private void showDetail(PaymentDto payment) {
        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        detailContainer.getChildren().clear();

        Label title = new Label("Order summary");
        title.setStyle("-fx-font-size:20; -fx-font-weight:bold;");

        Label orderId = new Label("Order: #" + payment.getId());
        Label orderDate = new Label("Date: " + payment.getCreatedAt().format(DISPLAY_FORMAT));
        Label orderTotal = new Label("Total: " + payment.getAmount().toPlainString() + "€");

        String tipText = payment.getTip() != null
                ? payment.getTip().stripTrailingZeros().toPlainString() + "%"
                : "-";
        Label tip = new Label("Tip: " + tipText);

        String methodText = payment.getPaymentMethodName() != null
                ? payment.getPaymentMethodName()
                : "-";
        Label paymentMethod = new Label("Payment: " + methodText);

        detailContainer.getChildren().addAll(title, orderId, orderDate, orderTotal, tip, paymentMethod);
    }

    @FXML
    private void closeDetail() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }
}