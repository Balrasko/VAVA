package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class HistoryController {

    @FXML
    private BorderPane root;

    @FXML
    private VBox ordersContainer;

    @FXML
    public void initialize() {

        addDay("Today");

        addOrder("#23","25.2.2025 14:00","306.43€");
        addOrder("#22","25.2.2025 13:12","54.20€");

        addDay("Yesterday");

        addOrder("#21","24.2.2025 18:45","18.90€");
    }

    private void addDay(String day){

        Label label = new Label(day);
        label.setStyle("-fx-font-size:16; -fx-font-weight:bold;");

        ordersContainer.getChildren().add(label);
    }

        private void addOrder(String id, String date, String total){

        HBox card = new HBox(20);

        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-padding:15;" +
                "-fx-background-radius:12;"
        );

        // kurzor ruky
        card.setCursor(javafx.scene.Cursor.HAND);

        // hover efekt
        card.setOnMouseEntered(e ->
            card.setStyle(
                "-fx-background-color:#f3f4f6;" +
                "-fx-padding:15;" +
                "-fx-background-radius:12;"
            )
        );

        card.setOnMouseExited(e ->
            card.setStyle(
                "-fx-background-color:white;" +
                "-fx-padding:15;" +
                "-fx-background-radius:12;"
            )
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

    private void showDetail(String id, String date, String total){

        VBox detail = new VBox(15);

        detail.setPrefWidth(420);

        detail.setStyle(
                "-fx-padding:25;" +
                "-fx-background-color:white;" +
                "-fx-background-radius:15;"
        );

        /* HEADER */

        Label title = new Label("Order summary");
        title.setStyle("-fx-font-size:16; -fx-font-weight:bold;");

        Button close = new Button("X");
        close.setStyle("-fx-background-color:transparent;");

        close.setOnAction(e -> root.setRight(null));

        HBox header = new HBox(title, close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(260);

        /* ORDER DATA */

        Label orderId = new Label("Order: " + id);
        Label orderDate = new Label("Date: " + date);
        Label orderTotal = new Label("Total: " + total);

        /* ACTION BUTTONS */

        Button print = new Button("Print");
        Button refund = new Button("Refund");

        HBox actions = new HBox(10, print, refund);

        detail.getChildren().addAll(
                header,
                orderId,
                orderDate,
                orderTotal,
                actions
        );

        root.setRight(detail);
    }
}