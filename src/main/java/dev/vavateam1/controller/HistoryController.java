package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class HistoryController {

    @FXML
    private VBox ordersContainer;

    @FXML
    private VBox detailPanel;

    @FXML
    public void initialize() {

        addDay("Today");

        addOrder("#23","25.2.2025 14:00","306.43€");
        addOrder("#22","25.2.2025 13:12","54.20€");

        addDay("Yesterday");

        addOrder("#21","24.2.2025 18:45","18.90€");
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }

        private void addDay(String day){

        Label label = new Label(day);

        label.setStyle(
                "-fx-background-color:white;" +
                "-fx-padding:8 18;" +
                "-fx-background-radius:20;" +
                "-fx-font-weight:bold;"
        );

        ordersContainer.getChildren().add(label);
        }

        private void addOrder(String id, String date, String total){

        HBox card = new HBox(20);

        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-padding:15 25;" +
                "-fx-background-radius:25;"
        );

        Label orderId = new Label(id);
        Label orderDate = new Label(date);
        Label orderTotal = new Label(total);

        orderTotal.setStyle("-fx-font-weight:bold;");

        HBox.setHgrow(orderDate, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(orderId, orderDate, orderTotal);

        card.setOnMouseClicked(e -> showDetail(id,date,total));

        ordersContainer.getChildren().add(card);
        }

    @FXML
    private VBox detailContainer;

    private void showDetail(String id, String date, String total){

        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        detailContainer.getChildren().clear();

        Label title = new Label("Order summary");
        title.setStyle("-fx-font-size:20; -fx-font-weight:bold;");

        Label orderId = new Label("Order: " + id);
        Label orderDate = new Label("Date: " + date);
        Label orderTotal = new Label("Total: " + total);

        Label tip = new Label("Tip: %");
        Label payment = new Label("Payment: card");

        detailContainer.getChildren().addAll(
                title,
                orderId,
                orderDate,
                orderTotal,
                tip,
                payment
        );
    }

    @FXML
    private void closeDetail() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
    }
}
