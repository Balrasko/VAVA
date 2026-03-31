package dev.vavateam1.controller;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.OrderItemView;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import dev.vavateam1.service.MockOrderService;;


// Payment screen needs buttons for discount, tip, actual payment.


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

    @FXML
    private Button splitButton;

    private Button selectedCategory;
    private DashboardController dashboardController;

    private boolean splitBillMode = false;

    private Table table;
    private List<MenuItem> menuItems;
    private List<Category> categories;
    private List<OrderItemView> orderItemViews;

    // TEMPORARY
    private BigDecimal subtotal = new BigDecimal(0);

    public void initData(Table table, DashboardController dashboardController) {
        this.categories = orderService.getCategories();
        this.menuItems = orderService.getMenuItems();
        this.orderItemViews = getOrderItemViews(orderService.getOrderItems(table));
        this.table = table;
        this.dashboardController = dashboardController;

        this.tableLabel.setText("Table " + table.getTableNumber());

        loadCategories();
        loadOrderItems();
    }

    private List<OrderItemView> getOrderItemViews(List<OrderItem> orderItems) {
        List<OrderItemView> orderItemViews = new ArrayList<>();
        for (OrderItem item : orderItems) {
            MenuItem menuItem = menuItems.stream().filter(m -> m.getId() == item.getMenuItemId()).findFirst().orElse(null);

            orderItemViews.add(new OrderItemView(item, menuItem));
        }

        return orderItemViews;
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

        // Show available items from the category
        menuItems.stream()
            .filter(item -> (item.getCategoryId() == categoryId && item.getAvailability()))
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

        card.setOnMouseClicked(e -> addItemToOrder(item));

        return card;
    }

    private void addItemToOrder(MenuItem menuItem) {
        OrderItemView existingItem = orderItemViews.stream()
            .filter(item -> item.isSameItem(menuItem, null))
            .findFirst()
            .orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
            
            refreshOrderPanel();
        }
        else {
            OrderItemView newItem = new OrderItemView(orderService.createOrderFromMenu(menuItem), menuItem);

            orderItemViews.add(newItem);
            orderPanel.getChildren().add(createOrderItemRow(newItem));

            subtotal = subtotal.add(menuItem.getPrice());
            updateTotals();
        }
    }

    private void refreshOrderPanel() {
        orderPanel.getChildren().clear();

        subtotal = BigDecimal.ZERO;

        for (OrderItemView item : orderItemViews) {
            orderPanel.getChildren().add(createOrderItemRow(item));
            subtotal = subtotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        updateTotals();
    }

    private void updateTotals() {
        subtotalLabel.setText(subtotal.toString() + " €");
    }

    private HBox createOrderItemRow(OrderItemView item) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(50);
        row.getStyleClass().add("order-row");

        // CheckBox
        CheckBox checkBox = new CheckBox();
        checkBox.setVisible(splitBillMode);

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
        //minusBtn.setStyle("-fx-background-color: #dd5656");

        // Quantity value
        Label quantityValue = new Label(item.getQuantity().toString());

        // Minus button functionality
        minusBtn.setOnAction(e -> {
            var quantity = item.getQuantity();

            quantity--;
            subtotal = subtotal.subtract(item.getPrice());
            updateTotals();

            if (quantity <= 0) {
                orderPanel.getChildren().remove(row);
                orderItemViews.remove(item);
                return;
            }

            item.setQuantity(quantity);
            refreshOrderPanel();
        });

        // Plus button
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("quantity-button");
        //plusBtn.setStyle("-fx-background-color: #37ff4b");

        // Plus button functionality
        plusBtn.setOnAction(e -> {
            var quantity = item.getQuantity();

            quantity++;
            subtotal = subtotal.add(item.getPrice());
            updateTotals();

            item.setQuantity(quantity);
            refreshOrderPanel();
        });

        // Note button
        Button noteBtn = new Button(item.getNote() == null ? "Add note" : "Edit note");
        noteBtn.setPrefWidth(70);
        noteBtn.getStyleClass().add("note-button");

        // Note Popup
        ContextMenu notePopup = new ContextMenu();

        // Editable TextField
        TextField noteField = new TextField();
        noteField.setText(item.getNote() == null ? "" : item.getNote());
        noteField.setPromptText("Enter note...");

        // Save button for the text
        Button noteSaveBtn = new Button("Save");
        noteSaveBtn.getStyleClass().add("note-button");

        HBox noteBox = new HBox(5, noteField, noteSaveBtn);
        noteBox.setAlignment(Pos.CENTER_LEFT);

        // Save the note
        noteSaveBtn.setOnAction(e -> {
            String newNote = noteField.getText();

            if (newNote.isBlank()) {
                item.setNote(null);
            }
            else {
                item.setNote(newNote);
            }

            notePopup.hide();
            refreshOrderPanel();
        });

        // Also save by pressing "Enter"
        noteField.setOnAction(e -> noteSaveBtn.fire());

        CustomMenuItem noteCMI = new CustomMenuItem(noteBox);
        noteCMI.setHideOnClick(false);
        noteCMI.getStyleClass().add("no-hover-menu-item");

        notePopup.getItems().add(noteCMI);

        // Show the popup
        noteBtn.setOnAction(e -> {
            if (!notePopup.isShowing()) {
                notePopup.show(noteBtn, Side.BOTTOM, 0, 0);
            }
            else {
                notePopup.hide();
            }
        });

        // Discount
        Button discountBtn = new Button("Discount");
        discountBtn.getStyleClass().add("note-button");
        discountBtn.setPrefWidth(70);

        // Discount popup
        ContextMenu discountPopup = new ContextMenu();

        Label discountLabel = new Label("Current discount: " + item.getDiscount().toString() + " €");

        CustomMenuItem discountCMI = new CustomMenuItem(discountLabel);
        discountCMI.setHideOnClick(false);
        discountCMI.getStyleClass().add("no-hover-menu-item");

        discountPopup.getItems().add(discountCMI);

        discountBtn.setOnAction(e -> {
            if (!discountPopup.isShowing()) {
                discountPopup.show(discountBtn, Side.BOTTOM, 0, 0);
            }
            else {
                discountPopup.hide();
            }
        });
    
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Price
        Label price = new Label((item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))).toString() + " €");
        price.setMinWidth(50);

        row.getChildren().addAll(checkBox, name, quantityTextLabel, minusBtn, quantityValue, plusBtn, noteBtn, discountBtn, spacer, price);

        return row;
    }

    @FXML
    private void toggleSplitTheBill() {
        splitBillMode = !splitBillMode;

        if (!splitBillMode) {
            splitButton.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #000");
        }
        else {
            splitButton.setStyle("-fx-background-color: #7997E1; -fx-text-fill: #f4f4f4");
        }

        refreshOrderPanel();
    }

    private void loadOrderItems() {
        for (OrderItemView orderItemView : orderItemViews) {
            orderPanel.getChildren().add(createOrderItemRow(orderItemView));
            subtotal = subtotal.add(orderItemView.getPrice().multiply(BigDecimal.valueOf(orderItemView.getQuantity())));
        }

        updateTotals();
    }

    @FXML
    public void backToTableView() {
        // Return to the table page

        try {
            orderService.saveOrder(); // Save order
            dashboardController.showTableView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
