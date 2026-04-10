package dev.vavateam1.controller;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.model.Category;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dev.vavateam1.service.OrderService;
import com.google.inject.Inject;

public class OrderController {

    @Inject
    private OrderService orderService;

    @FXML
    private StackPane rootStack;

    @FXML
    private StackPane paymentOverlay;

    @FXML
    private VBox orderPanel;

    @FXML
    private Label tableLabel;

    @FXML
    private HBox categoryBox;

    @FXML
    private StackPane menuContainer;

    @FXML
    private TilePane menuTile;

    @FXML
    private Label totalLabelText;
    
    @FXML
    private Label totalLabel;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Button splitButton;

    @FXML
    private Button paymentButton;

    private Button selectedCategory;
    private DashboardController dashboardController;

    private boolean splitBillMode = false;

    private Table table;
    private List<MenuItem> menuItems;
    private List<Category> categories;
    private List<OrderItemDto> orderItemViews;
    private Map<OrderItemDto, Integer> selectedQuantities = new HashMap<>();
    private BigDecimal tip = BigDecimal.ZERO;

    private Label pluDisplay;
    private boolean pluOpen = false;
    private GridPane pluKeyboard;

    @FXML
    private Button pluButton;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public void initData(Table table, DashboardController dashboardController) {
        this.categories = orderService.getCategories(); // Get all categories
        this.menuItems = orderService.getMenuItems(); // Get all menu items
        this.orderItemViews = getOrderItemViews(orderService.getOrderItems(table)); // Get order items for the table if
                                                                                    // there are any
        this.table = table;
        this.dashboardController = dashboardController;

        this.tableLabel.setText("Table " + table.getTableNumber());

        this.totalLabelText.setVisible(splitBillMode);
        this.totalLabel.setVisible(splitBillMode);
        loadCategories();
        loadOrderItems();
    }

    private List<OrderItemDto> getOrderItemViews(List<OrderItem> orderItems) {
        // Get available order items for the current table if there are any

        List<OrderItemDto> orderItemViews = new ArrayList<>();
        for (OrderItem item : orderItems) {
            MenuItem menuItem = menuItems.stream().filter(m -> m.getId() == item.getMenuItemId()).findFirst()
                    .orElseThrow(() -> new RuntimeException("MenuItem not found."));

            orderItemViews.add(new OrderItemDto(item, menuItem));
        }

        return orderItemViews;
    }

    private void loadCategories() {
        // Load all the categories and make a button for each one

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
                .filter(item -> (item.getCategoryId() == categoryId && item.isAvailability()))
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
        // Add the menu item to the order as an order item

        OrderItemDto existingItem = orderItemViews.stream()
                .filter(item -> item.isSameItem(menuItem, null))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);

            refreshOrderPanel();
        } else {
            OrderItemDto newItem = new OrderItemDto(orderService.createOrderFromMenu(menuItem, table), menuItem);

            this.selectedQuantities.put(newItem, newItem.getQuantity());

            orderItemViews.add(newItem);
            orderPanel.getChildren().add(createOrderItemRow(newItem));

            subtotal = subtotal.add(menuItem.getPrice());
            total = calculateSplitSubtotal();
            updateTotals();
        }
    }

    private void refreshOrderPanel() {
        // Refresh the order panel UI

        orderPanel.getChildren().clear();

        subtotal = BigDecimal.ZERO;
        total = BigDecimal.ZERO;

        for (OrderItemDto item : orderItemViews) {
            orderPanel.getChildren().add(createOrderItemRow(item));
            subtotal = subtotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        total = calculateSplitSubtotal();

        updateTotals();
    }

    private void updateTotals() {

        totalLabel.setText(total.toString() + " €");

        subtotalLabel.setText(subtotal.toString() + " €");
    }

    private HBox createOrderItemRow(OrderItemDto item) {
        // Create a row for each order item in the UI

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(50);
        row.getStyleClass().add("order-row");

        // CheckBox
        CheckBox checkBox = new CheckBox();
        checkBox.setVisible(splitBillMode);
        checkBox.setOnAction(e -> {
            if (checkBox.isSelected()) {
                selectedQuantities.put(item, item.getQuantity());
            } else {
                selectedQuantities.put(item, 0);
            }

            refreshOrderPanel();
        });

        // Name label
        Label name = new Label(item.getName());
        // HBox.setHgrow(name, Priority.ALWAYS);
        name.setPrefWidth(70);
        name.setWrapText(true);

        // Quantity label
        Label quantityTextLabel = new Label("Quantity:");
        quantityTextLabel.setPrefWidth(50);

        // Minus button
        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("quantity-button");

        // Quantity value
        Label quantityValue = new Label(
                splitBillMode ? selectedQuantities.getOrDefault(item, 0) + " / " + item.getQuantity()
                        : String.valueOf(item.getQuantity()));

        // Minus button functionality
        minusBtn.setOnAction(e -> {
            if (splitBillMode) {
                int selected = selectedQuantities.getOrDefault(item, 0);

                if (selected > 0) {
                    selectedQuantities.put(item, selected - 1);
                }
            } else {
                int quantity = item.getQuantity() - 1;

                if (quantity <= 0) {
                    if (item.getOrderItemId() != null) {
                        orderService.deleteOrderItem(item.getOrderItemId());
                    }
                    orderItemViews.remove(item);
                    selectedQuantities.remove(item);
                } else {
                    item.setQuantity(quantity);
                    selectedQuantities.put(item, quantity);
                }
            }

            refreshOrderPanel();
        });

        // Plus button
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("quantity-button");

        // Plus button functionality
        plusBtn.setOnAction(e -> {
            if (splitBillMode) {
                int selected = selectedQuantities.getOrDefault(item, 0);

                if (selected < item.getQuantity()) {
                    selectedQuantities.put(item, selected + 1);
                }
            } else {
                item.setQuantity(item.getQuantity() + 1);
                selectedQuantities.put(item, item.getQuantity());
            }

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
            } else {
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
            } else {
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
            } else {
                discountPopup.hide();
            }
        });

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Price
        Label price = new Label((item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))).toString() + " €");
        price.setMinWidth(50);

        row.getChildren().addAll(checkBox, name, quantityTextLabel, minusBtn, quantityValue, plusBtn, noteBtn,
                discountBtn, spacer, price);

        int selected = selectedQuantities.getOrDefault(item, 0);
        checkBox.setSelected(selected == item.getQuantity());

        return row;
    }

    @FXML
    private void toggleSplitTheBill() {
        // Toggle split the bill mode

        splitBillMode = !splitBillMode;

        if (!splitBillMode) {
            for (OrderItemDto item : orderItemViews) {
                selectedQuantities.put(item, item.getQuantity());
            }
            splitButton.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #000");
        } else {
            for (OrderItemDto item : orderItemViews) {
                selectedQuantities.put(item, 0);
            }
            splitButton.setStyle("-fx-background-color: #7997E1; -fx-text-fill: #f4f4f4");
        }

        totalLabel.setVisible(splitBillMode);
        totalLabelText.setVisible(splitBillMode);
        refreshOrderPanel();
    }

    private void loadOrderItems() {
        for (OrderItemDto orderItemView : orderItemViews) {
            orderPanel.getChildren().add(createOrderItemRow(orderItemView));
            subtotal = subtotal.add(orderItemView.getPrice().multiply(BigDecimal.valueOf(orderItemView.getQuantity())));
            this.selectedQuantities.put(orderItemView, orderItemView.getQuantity());
        }

        total = calculateSplitSubtotal();
        updateTotals();
    }

    private List<OrderItemDto> getItemsForPayment() {
        // Get a list of currently selected order items with their quantities - relevant
        // mainly in split the bill mode

        List<OrderItemDto> result = new ArrayList<>();

        for (OrderItemDto item : orderItemViews) {
            int quantity;

            if (splitBillMode) {
                quantity = selectedQuantities.getOrDefault(item, 0);
            } else {
                quantity = item.getQuantity();
            }

            if (quantity <= 0)
                continue;

            OrderItemDto copy = item.copy();
            copy.setQuantity(quantity);

            result.add(copy);
        }

        return result;
    }

    private BigDecimal calculateSplitSubtotal() {
        // Calculate the subtotal value of currenlty selected items in "split the bill" mode

        BigDecimal sum = BigDecimal.ZERO;

        for (OrderItemDto item : orderItemViews) {
            int quantity = selectedQuantities.get(item);

            BigDecimal splitSubtotal = item.getPrice().multiply(BigDecimal.valueOf(quantity));
            sum = sum.add(splitSubtotal);
        }

        return sum;
    }

    private BigDecimal calculatePaymentSubtotal(List<OrderItemDto> items) {
        // Calculate the subtotal value of the currently selected items - mainly
        // relevant for split the bill mode

        BigDecimal sum = BigDecimal.ZERO;

        for (OrderItemDto item : items) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            sum = sum.add(itemTotal);
        }

        return sum;
    }

    @FXML
    private void showPaymentOverlay() {
        // Show a payment popup window

        // Get the selected items
        List<OrderItemDto> itemsToPay = getItemsForPayment();

        // Return if there are no items in the order
        if (itemsToPay.isEmpty())
            return;

        Region overlayBg = new Region();
        overlayBg.setStyle("-fx-background-color: #d9d9d9;");
        overlayBg.prefWidthProperty().bind(rootStack.widthProperty());
        overlayBg.prefHeightProperty().bind(rootStack.heightProperty());

        VBox paymentBox = new VBox(15);
        paymentBox.setAlignment(Pos.CENTER);
        StackPane.setMargin(paymentBox, new Insets(60, 0, 60, 0));
        paymentBox.setMaxWidth(700);
        paymentBox.setStyle("""
                    -fx-background-color: #fff;
                    -fx-padding: 20;
                    -fx-background-radius: 10;
                """);

        // Money values
        VBox orderInfo = new VBox(15);

        // Buttons
        VBox paymentButtons = new VBox(25);
        paymentButtons.setAlignment(Pos.BOTTOM_RIGHT);

        HBox topRow = new HBox(30);
        HBox bottomRow = new HBox(30);

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox bottomPart = new HBox();
        bottomPart.setAlignment(Pos.CENTER_LEFT);

        Region horizontalSpacer = new Region();
        HBox.setHgrow(horizontalSpacer, Priority.ALWAYS);

        // List of selected order items for the payment
        VBox receiptList = new VBox(8);

        ScrollPane scrollPane = new ScrollPane(receiptList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        // Add all the selected items to the receipt
        for (OrderItemDto item : itemsToPay) {
            HBox row = new HBox(10);

            Label name = new Label(item.getName() + " x" + item.getQuantity());

            BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            Label price = new Label("€" + totalPrice.toString());

            Region receiptSpacer = new Region();
            HBox.setHgrow(receiptSpacer, Priority.ALWAYS);

            row.getChildren().addAll(name, receiptSpacer, price);

            receiptList.getChildren().add(row);

            if (item.getNote() != null) {
                Label note = new Label("  - " + item.getNote());
                receiptList.getChildren().add(note);
            }
        }

        // Popup title
        Label title = new Label("Table " + this.table.getTableNumber() + " Order Summary");
        title.setStyle("""
                    -fx-font-size: 36px;
                    -fx-padding: 8;
                """);

        // Tip
        Button tipBtn = new Button("Tip: " + tip.toString() + "%");
        tipBtn.getStyleClass().add("note-button");
        tipBtn.setStyle("""
                    -fx-font-size: 26px;
                    -fx-padding: 8;
                """);

        // Calculate subtotal for the selected items
        BigDecimal paymentSubtotal = calculatePaymentSubtotal(itemsToPay);

        // Calculate total -> subtotal + subtotal * tip
        total = paymentSubtotal.multiply(tip).divide(new BigDecimal(100)).add(paymentSubtotal);

        Label totalLabel = new Label("Total: €" + total.toString());
        totalLabel.setStyle("""
                    -fx-font-size: 26px;
                """);

        Label subtotalLabel = new Label("Subtotal: €" + paymentSubtotal);
        subtotalLabel.setStyle("""
                    -fx-font-size: 24px;
                """);

        // Tip Popup
        ContextMenu tipPopup = new ContextMenu();

        // Editable TextField
        TextField tipField = new TextField();
        tipField.setPromptText("Tip...%");

        // Save button for the text
        Button tipSaveBtn = new Button("Save");
        tipSaveBtn.getStyleClass().add("note-button");

        HBox tipBox = new HBox(5, tipField, tipSaveBtn);
        tipBox.setAlignment(Pos.CENTER_LEFT);

        // Save the tip
        tipSaveBtn.setOnAction(e -> {
            String newTip = tipField.getText();

            try {
                tip = newTip.isBlank() ? BigDecimal.ZERO : new BigDecimal(newTip);
            } catch (NumberFormatException ex) {
                tip = BigDecimal.ZERO;
            }

            // Don't allow a negative tip
            tip = tip.max(BigDecimal.ZERO);

            tipBtn.setText("Tip: " + tip.toString() + "%");
            total = paymentSubtotal.multiply(tip).divide(new BigDecimal(100)).add(paymentSubtotal);
            totalLabel.setText("Total: €" + total.toString());
            tipPopup.hide();
        });

        // Also save by pressing "Enter"
        tipField.setOnAction(e -> tipSaveBtn.fire());

        CustomMenuItem tipCMI = new CustomMenuItem(tipBox);
        tipCMI.setHideOnClick(false);
        tipCMI.getStyleClass().add("no-hover-menu-item");

        tipPopup.getItems().add(tipCMI);

        // Show the popup
        tipBtn.setOnAction(e -> {
            if (!tipPopup.isShowing()) {
                tipPopup.show(tipBtn, Side.BOTTOM, 0, 0);
            } else {
                tipPopup.hide();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("note-button");
        cancelBtn.setStyle("-fx-font-size: 24;");

        Button cardPaymentBtn = new Button("Card Payment");
        cardPaymentBtn.getStyleClass().add("note-button");
        cardPaymentBtn.setStyle("-fx-font-size: 24;");
        // Perform payment
        cardPaymentBtn.setOnAction(e -> processPayment(2, total, tip));

        Button cashPaymentBtn = new Button("Cash Payment");
        cashPaymentBtn.getStyleClass().add("note-button");
        cashPaymentBtn.setStyle("-fx-font-size: 24;");
        // Perform payment
        cashPaymentBtn.setOnAction(e -> processPayment(1, total, tip));

        orderInfo.getChildren().addAll(tipBtn, totalLabel, subtotalLabel);

        topRow.getChildren().addAll(cardPaymentBtn, buttonSpacer);
        bottomRow.getChildren().addAll(cashPaymentBtn, cancelBtn);

        paymentButtons.getChildren().addAll(topRow, bottomRow);

        bottomPart.getChildren().addAll(orderInfo, horizontalSpacer, paymentButtons);

        paymentBox.getChildren().addAll(title, scrollPane, spacer, bottomPart);

        paymentOverlay = new StackPane(overlayBg, paymentBox);

        // Close the popup without performing the payment
        cancelBtn.setOnAction(e -> {
            rootStack.getChildren().remove(paymentOverlay);
        });
        // Also close if you click outside the main popup window
        overlayBg.setOnMouseClicked(e -> rootStack.getChildren().remove(paymentOverlay));

        rootStack.getChildren().remove(paymentOverlay);
        rootStack.getChildren().add(paymentOverlay);
    }

    private void processPayment(int paymentMethod, BigDecimal totalPrice, BigDecimal tip) {

        List<OrderItem> ordersToProcess = new ArrayList<>();
        List<OrderItemDto> itemsToRemove = new ArrayList<>();

        for (OrderItemDto item : orderItemViews) {
            int selected = splitBillMode ? selectedQuantities.getOrDefault(item, 0) : item.getQuantity();

            if (selected == 0)
                continue;

            // Using split the bill mode
            if (selected < item.getQuantity()) {
                // Update item quantity
                item.setQuantity(item.getQuantity() - selected);

                // Make a new orderItem entry for the split amount
                OrderItemDto splitCopy = item.copy();
                splitCopy.setQuantity(selected);
                splitCopy.setOrderIdToNull();
                ordersToProcess.add(splitCopy.getOrderItem());
            } else { // Paying for the whole quantity
                ordersToProcess.add(item.getOrderItem());

                itemsToRemove.add(item);
            }
        }

        orderItemViews.removeAll(itemsToRemove);
        orderService.processPayment(ordersToProcess, paymentMethod, totalPrice, tip);
        rootStack.getChildren().remove(paymentOverlay);
        this.tip = BigDecimal.ZERO;
        refreshOrderPanel();
    }

    @FXML
    private void togglePluKeyboard() {
        if (pluOpen) {
            closePluKeyboard();
        } else {
            openPluKeyboard();
        }
    }

    private void openPluKeyboard() {
        if (pluKeyboard == null) {
            pluKeyboard = createPluKeyboard();
        }

        if (!menuContainer.getChildren().contains(pluKeyboard)) {
            menuContainer.getChildren().add(pluKeyboard);
            StackPane.setAlignment(pluKeyboard, Pos.CENTER_RIGHT);
            StackPane.setMargin(pluKeyboard, new Insets(10));
        }

        pluOpen = true;
    }

    private void closePluKeyboard() {
        menuContainer.getChildren().remove(pluKeyboard);
        pluDisplay.setText("");

        pluOpen = false;
    }

    private GridPane createPluKeyboard() {
        // Build the keyboard popup

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setMaxWidth(300);
        grid.setMaxHeight(Region.USE_PREF_SIZE);

        grid.setStyle("""
                    -fx-background-color: #e0e0e0;
                    -fx-background-radius: 15;
                """);

        // Columns
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        // Rows
        for (int i = 0; i < 5; i++) {
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(50);
            grid.getRowConstraints().add(row);
        }

        // Code display
        pluDisplay = new Label("");
        pluDisplay.setMaxWidth(Double.MAX_VALUE);
        pluDisplay.setMinHeight(50);
        pluDisplay.setStyle(
                "-fx-background-color: #fff; -fx-padding: 10; -fx-background-radius: 8; -fx-font-size: 18px;");

        grid.add(pluDisplay, 0, 0);
        GridPane.setColumnSpan(pluDisplay, 2);

        // Close button
        Button enterBtn = createPluButton("Enter");
        enterBtn.getStyleClass().add("plu-keyboard-button");
        enterBtn.setOnAction(e -> enterPluCode());
        grid.add(enterBtn, 2, 0);

        // Layout of the buttons
        String[][] layout = {
                { "1", "2", "3" },
                { "4", "5", "6" },
                { "7", "8", "9" },
                { "CLR", "0", "⌫" }
        };

        // Create a Button for each character from the layout using the layout's
        // position
        for (int row = 0; row < layout.length; row++) {
            for (int col = 0; col < layout[row].length; col++) {
                Button btn = createPluButton(layout[row][col]);
                grid.add(btn, col, row + 1);

                btn.setOnAction(r -> handlePluInput(btn.getText()));
            }
        }

        return grid;
    }

    private Button createPluButton(String text) {
        // Button factory

        Button btn = new Button(text);
        btn.getStyleClass().add("plu-keyboard-button");
        return btn;
    }

    private void handlePluInput(String input) {
        String current = pluDisplay.getText();

        switch (input) {
            case "CLR": // Clear the display
                pluDisplay.setText("");
                return;

            case "⌫": // Remove the last character from the display
                if (!current.isEmpty()) {
                    pluDisplay.setText(current.substring(0, current.length() - 1));
                }
                break;

            default: // Add the selected character to the display
                pluDisplay.setText(current + input);
                break;
        }
    }

    private void enterPluCode() {
        String code = pluDisplay.getText();
        
        try {
            addItemToOrder(orderService.getItemsByPluCode(code).getFirst());
            
        }
        catch (RuntimeException e) {
            System.out.println("Wrong code");
        }
    }

    @FXML
    public void backToTableView() {
        // Return to the table page

        try {
            orderService.saveTempOrders(orderItemViews); // Save order items for inclomplete order
            dashboardController.showTableView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
