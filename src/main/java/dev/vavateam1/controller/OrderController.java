package dev.vavateam1.controller;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import dev.vavateam1.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dev.vavateam1.service.MockOrderService;;

public class OrderController {

    private MockOrderService orderService = new MockOrderService();

    @FXML
    private VBox menuPanel;

    @FXML
    private VBox orderPanel;

    @FXML
    private Label orderLabel;

    private DashboardController dashboardController;

    private Table table;
    private List<MenuItem> menuItems;
    private List<Category> categories;
    private List<OrderItem> orderItems;

    public void initData(Table table, DashboardController dashboardController) {
        this.categories = orderService.getCategories();
        this.menuItems = orderService.getAvailableMenuItems();
        this.orderItems = orderService.getOrderItems(table);
        this.table = table;
        this.dashboardController = dashboardController;

        this.orderLabel.setText("Order view for Table " + table.getTableNumber());
    }
}
