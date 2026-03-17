package dev.vavateam1.controller;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import dev.vavateam1.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dev.vavateam1.service.MockOrderService;;



// RENAME CLOSING TO FINANCE
// RENAME CLOSING TO FINANCE
// RENAME CLOSING TO FINANCE
// RENAME CLOSING TO FINANCE
// RENAME CLOSING TO FINANCE
// RENAME CLOSING TO FINANCE



public class OrderController {

    private MockOrderService orderService = new MockOrderService();

    @FXML
    private VBox menuPanel;

    @FXML
    private VBox orderPanel;

    @FXML
    private Label tableLabel;

    @FXML
    private HBox categoryBox;

    @FXML
    private TilePane menuTile;


    private Button selectedCategory;
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

        this.tableLabel.setText("Table " + table.getTableNumber());

        loadCategories();
    }

    private void loadCategories() {
        // Load all the categories and make a button for each

        boolean first = true;

        for (Category category : categories) {
            Button btn = new Button(category.getName());

            btn.getStyleClass().add("category-button");

            btn.setOnAction(e -> {
                selectCategory(btn);
                
                showCategory(category.getId());
            });

            btn.setMinHeight(40);
            btn.setPrefWidth(140);
            btn.setWrapText(true);

            categoryBox.getChildren().add(btn);

            // Automatically select first loaded category
            if (first) {
                selectCategory(btn);
                showCategory(category.getId());

                first = false;
            }
        }
    }

    private void selectCategory(Button button) {
        if (this.selectedCategory != null) {
            this.selectedCategory.getStyleClass().remove("category-button-selected");

            if (!this.selectedCategory.getStyleClass().contains("category-button")) {
                this.selectedCategory.getStyleClass().add("category-button");
            }
        }

        button.getStyleClass().remove("category-button");
        button.getStyleClass().add("category-button-selected");

        this.selectedCategory = button;
    }

    private void showCategory(int categoryId) {
        menuTile.getChildren().clear();

        menuItems.stream()
            .filter(item -> item.getCategoryId() == categoryId)
            .forEach(item -> {
                VBox card = createMenuItemCard(item);
                menuTile.getChildren().add(card);
            });
    }

    private VBox createMenuItemCard(MenuItem item) {
        VBox card = new VBox(5);

        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-alignment: center;");
        
        card.getChildren().addAll(name);

        card.setPrefSize(100, 100);

        card.setStyle("""
            -fx-background-color: #d9d9d9;
            -fx-background-radius: 10;
            -fx-padding: 10;
            -fx-alignment: center;
            -fx-cursor: hand;
        """);

        // card.setOnMouseClicked(e -> addItemToOrder(item));

        return card;
    }

    @FXML
    public void backToTableView() {
        try {
            dashboardController.showTableView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
