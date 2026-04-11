package dev.vavateam1.controller;

import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dto.KitchenOrder;
import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.model.OrderStatus;
import dev.vavateam1.service.AuthService;
import dev.vavateam1.service.KitchenService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class KitchenController {

    @FXML
    private VBox firstColumn;

    @FXML
    private VBox secondColumn;

    @FXML
    private VBox thirdColumn;

    private final AuthService authService;
    private final KitchenService kitchenService;
    private final ViewSwitcher viewSwitcher;

    @Inject
    public KitchenController(AuthService authService, KitchenService kitchenService, ViewSwitcher viewSwitcher) {
        this.authService = authService;
        this.kitchenService = kitchenService;
        this.viewSwitcher = viewSwitcher;
    }

    @FXML
    public void initialize() {
        refreshOrders();
    }

    private void refreshOrders() {
        clearColumns();

        List<KitchenOrder> orders = kitchenService.getAllOrders();

        for (int index = 0; index < orders.size(); index++) {
            KitchenOrder order = orders.get(index);
            VBox targetColumn = getColumn(index % 3);
            targetColumn.getChildren().add(createOrderCard(order));
        }

        if (orders.isEmpty()) {
            firstColumn.getChildren().add(createEmptyState());
        }
    }

    private void clearColumns() {
        firstColumn.getChildren().clear();
        secondColumn.getChildren().clear();
        thirdColumn.getChildren().clear();
    }

    private VBox getColumn(int index) {
        return switch (index) {
            case 0 -> firstColumn;
            case 1 -> secondColumn;
            default -> thirdColumn;
        };
    }

    private VBox createOrderCard(KitchenOrder order) {
        VBox card = new VBox();
        card.getStyleClass().add("chef-card");
        if (order.getStatus() == OrderStatus.DONE) {
            card.getStyleClass().add("chef-card-done");
        }

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("chef-card-header");
        header.getStyleClass().add(getStatusStyleClass(order.getStatus()));

        VBox titleBox = new VBox(2);
        Label tableLabel = new Label("Table " + order.getTableNumber());
        tableLabel.getStyleClass().add("chef-card-title");

        Label orderLabel = new Label("Order #" + order.getId());
        orderLabel.getStyleClass().add("chef-card-subtitle");

        titleBox.getChildren().addAll(tableLabel, orderLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (order.getStatus() == OrderStatus.DONE) {
            Button deleteButton = new Button("DEL");
            deleteButton.getStyleClass().add("chef-delete-button");
            deleteButton.setOnAction(event -> {
                kitchenService.deleteDoneOrder(order.getId());
                refreshOrders();
            });
            header.getChildren().addAll(titleBox, spacer, deleteButton);
        } else {
            header.getChildren().addAll(titleBox, spacer);
        }

        VBox itemsBox = new VBox(10);
        itemsBox.getStyleClass().add("chef-card-body");
        for (OrderItemDto item : order.getItems()) {
            itemsBox.getChildren().add(createOrderItem(item, order.getStatus()));
        }

        Button statusButton = new Button("Close order");
        statusButton.getStyleClass().add("chef-status-button");
        boolean canCloseOrder = order.getStatus() == OrderStatus.DONE;
        statusButton.setDisable(!canCloseOrder);
        statusButton.setOnAction(event -> {
            kitchenService.deleteDoneOrder(order.getId());
            refreshOrders();
        });

        card.getChildren().add(header);
        card.getChildren().add(itemsBox);
        card.getChildren().add(statusButton);
        VBox.setMargin(itemsBox, new Insets(16, 18, 8, 18));
        VBox.setMargin(statusButton, new Insets(0, 18, 18, 18));

        return card;
    }

    private HBox createOrderItem(OrderItemDto item, OrderStatus status) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        VBox itemBox = new VBox(4);
        HBox.setHgrow(itemBox, Priority.ALWAYS);

        Label itemLabel = new Label(item.getQuantity() + "x " + item.getName());
        itemLabel.getStyleClass().add("chef-item-label");
        itemLabel.setWrapText(true);

        if (status == OrderStatus.DONE) {
            itemLabel.getStyleClass().add("chef-item-done");
        }

        itemBox.getChildren().add(itemLabel);

        if (item.getNote() != null && !item.getNote().isBlank()) {
            Label noteLabel = new Label(item.getNote());
            noteLabel.getStyleClass().add("chef-note-label");
            noteLabel.setWrapText(true);
            if (status == OrderStatus.DONE) {
                noteLabel.getStyleClass().add("chef-item-done");
            }
            itemBox.getChildren().add(noteLabel);
        }

        Button doneButton = new Button("Done");
        doneButton.getStyleClass().add("chef-item-done-button");
        boolean itemDone = item.getOrderItem() != null && "DONE".equalsIgnoreCase(item.getOrderItem().getStatus());
        doneButton.setDisable(itemDone);
        doneButton.setOnAction(event -> {
            Integer orderItemId = item.getOrderItemId();
            if (orderItemId != null) {
                kitchenService.markOrderItemDone(orderItemId);
                refreshOrders();
            }
        });

        row.getChildren().addAll(itemBox, doneButton);
        return row;
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(32));
        emptyState.getStyleClass().add("chef-empty-state");

        Label title = new Label("No kitchen orders");
        title.getStyleClass().add("chef-empty-title");

        Label subtitle = new Label("Done orders deleted with DEL will disappear from the board.");
        subtitle.getStyleClass().add("chef-empty-subtitle");
        subtitle.setWrapText(true);

        emptyState.getChildren().addAll(title, subtitle);
        return emptyState;
    }

    private String getStatusStyleClass(OrderStatus status) {
        return switch (status) {
            case RECEIVED -> "chef-status-received";
            case IN_PROGRESS -> "chef-status-progress";
            case DONE -> "chef-status-done";
        };
    }
    @FXML
    private void handleLogout() throws Exception {
        authService.logout();
        viewSwitcher.SetView("/view/login.fxml");
    }
}
