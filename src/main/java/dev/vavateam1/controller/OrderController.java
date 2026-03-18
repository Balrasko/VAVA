package dev.vavateam1.controller;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import dev.vavateam1.model.Category;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

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
    private VBox orderPanel;

    @FXML
    private Label tableLabel;

    @FXML
    private HBox categoryBox;

    @FXML
    private TilePane menuTile;

    @FXML
    private Label totalLabel;

    @FXML
    private Label subtotalLabel;

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
        loadMockOrderUI();
    }

    private void loadCategories() {
        // Load all the categories and make a button for eachq one

        boolean first = true;

        for (Category category : categories) {
            Button btn = new Button(category.getName());

            btn.getStyleClass().add("category-button");

            btn.setOnAction(e -> {
                selectCategory(btn);
                
                showCategory(category.getId());
            });

            btn.setWrapText(true);
            btn.setTextAlignment(TextAlignment.CENTER);
            btn.setMinHeight(60);
            btn.setPrefWidth(140);
            btn.setStyle("-fx-cursor: hand;");

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
        // Highlight selected category button

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
        // Get menu items for the selected category

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

        card.setPrefSize(95, 95);

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

    private HBox createOrderItemRow(MenuItem item) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(50);
        row.getStyleClass().add("order-row");

        // CheckBox
        CheckBox checkBox = new CheckBox();

        // Name label
        Label name = new Label(item.getName());
        //HBox.setHgrow(name, Priority.ALWAYS);
        name.setPrefWidth(70);
        name.setWrapText(true);

        // Quantity label
        Label quantityTextLabel = new Label("Quantity:");
        quantityTextLabel.setPrefWidth(50);

        // Minus button
        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("quantity-button");
        minusBtn.setStyle("-fx-background-color: #dd5656");

        // Quantity value
        Label quantityValue = new Label("1");
        // get the quantity from OrderItem

        // Plus button
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("quantity-button");
        plusBtn.setStyle("-fx-background-color: #37ff4b");

        // Note button
        Button noteBtn = new Button("Add note");
        noteBtn.setMinWidth(70);
        noteBtn.getStyleClass().add("note-button");

        // Discount
        Label discountText = new Label("Discount:");
        discountText.setPrefWidth(50);
        Label discountValue = new Label("0.00");
        discountValue.setMinWidth(50);
    
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.NEVER);

        // Price
        Label price = new Label(item.getPrice().toString() + " €");
        price.setMinWidth(50);

        row.getChildren().addAll(checkBox, name, quantityTextLabel, minusBtn, quantityValue, plusBtn, noteBtn, discountText, discountValue, spacer, price);

        return row;
    }

    private void loadMockOrderUI() {
        orderPanel.getChildren().clear();

        // Just reuse your menu items for now
        menuItems.stream().limit(4).forEach(item -> {
            orderPanel.getChildren().add(createOrderItemRow(item));
    });
}

    @FXML
    public void backToTableView() {
        // Return to the table page

        try {
            dashboardController.showTableView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
