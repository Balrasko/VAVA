package dev.vavateam1.controller;

import com.google.inject.Inject;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.service.MenuService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MenuController {

    private final MenuService menuService;

    @FXML private Button foodTabButton;
    @FXML private Button drinksTabButton;
    @FXML private Button servicesTabButton;

    @FXML private VBox foodSection;
    @FXML private VBox drinksSection;
    @FXML private VBox servicesSection;

    @FXML private VBox menuFormPanel;

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField discountField;

    private String selectedCategory = "food";

    @Inject
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @FXML
    public void initialize() {
        setActiveTab("food");
        showSection("food");
        hideForm();
        loadItems("food");
    }

    @FXML
    private void onFoodTab() {
        selectedCategory = "food";
        setActiveTab("food");
        showSection("food");
        loadItems("food");
    }

    @FXML
    private void onDrinksTab() {
        selectedCategory = "drinks";
        setActiveTab("drinks");
        showSection("drinks");
        loadItems("drinks");
    }

    @FXML
    private void onServicesTab() {
        selectedCategory = "services";
        setActiveTab("services");
        showSection("services");
        loadItems("services");
    }

    @FXML
    private void onAddMenu() {
        clearForm();
        categoryField.setText(selectedCategory);
        showForm();
    }

    @FXML
    private void onCloseForm() {
        hideForm();
    }

    @FXML
    private void onSubmitMenu() {
        MenuItem newItem = new MenuItem(
                0,
                mapCategoryToId(categoryField.getText()),
                generateItemCode(),
                nameField.getText(),
                new BigDecimal(priceField.getText().isBlank() ? "0.00" : priceField.getText()),
                true,
                descriptionField.getText(),
                isKitchenCategory(categoryField.getText()),
                new BigDecimal(discountField.getText().isBlank() ? "0.00" : discountField.getText()),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        menuService.addMenuItem(newItem);
        hideForm();
        loadItems(selectedCategory);
    }


    private void showForm() {
        menuFormPanel.setVisible(true);
        menuFormPanel.setManaged(true);
    }

    private void hideForm() {
        menuFormPanel.setVisible(false);
        menuFormPanel.setManaged(false);
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        categoryField.clear();
        priceField.clear();
        discountField.clear();
    }

    private void showSection(String section) {
        boolean food = "food".equals(section);
        boolean drinks = "drinks".equals(section);
        boolean services = "services".equals(section);

        foodSection.setVisible(food);
        foodSection.setManaged(food);

        drinksSection.setVisible(drinks);
        drinksSection.setManaged(drinks);

        servicesSection.setVisible(services);
        servicesSection.setManaged(services);
    }

    private void setActiveTab(String tabName) {
        foodTabButton.getStyleClass().remove("menu-tab-active");
        drinksTabButton.getStyleClass().remove("menu-tab-active");
        servicesTabButton.getStyleClass().remove("menu-tab-active");

        switch (tabName) {
            case "food" -> foodTabButton.getStyleClass().add("menu-tab-active");
            case "drinks" -> drinksTabButton.getStyleClass().add("menu-tab-active");
            case "services" -> servicesTabButton.getStyleClass().add("menu-tab-active");
        }
    }

    private int mapCategoryToId(String category) {
        return switch (category.toLowerCase()) {
            case "food" -> 1;
            case "drinks" -> 2;
            case "services" -> 3;
            default -> 0;
        };
    }

    private boolean isKitchenCategory(String category) {
        return "food".equalsIgnoreCase(category);
    }

    private int generateItemCode() {
        return (int) (100 + Math.random() * 900);
    }

    private void loadItems(String category) {
        List<MenuItem> items = menuService.getMenuItemsByCategory(category);
        renderItems(category, items);
    }

    private void renderItems(String category, List<MenuItem> items) {
        VBox targetSection = getSectionByCategory(category);
        if (targetSection == null) {
            return;
        }

        targetSection.getChildren().clear();

        for (MenuItem item : items) {
            VBox itemBox = createMenuItemNode(item);
            targetSection.getChildren().add(itemBox);
        }
    }

    private VBox getSectionByCategory(String category) {
        return switch (category) {
            case "food" -> foodSection;
            case "drinks" -> drinksSection;
            case "services" -> servicesSection;
            default -> null;
        };
    }

    private VBox createMenuItemNode(MenuItem item) {
        VBox wrapper = new VBox(12);

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("menu-item-title");

        Label descriptionLabel = new Label(
                item.getDescription() != null ? item.getDescription() : ""
        );
        descriptionLabel.getStyleClass().add("menu-item-description");

        VBox textBox = new VBox(3, nameLabel, descriptionLabel);
        HBox.setHgrow(textBox, javafx.scene.layout.Priority.ALWAYS);

        Label priceLabel = new Label(item.getPrice() != null ? item.getPrice().toString() + " €" : "");
        priceLabel.getStyleClass().add("menu-action-label");

        HBox row = new HBox(20, textBox, priceLabel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        wrapper.getChildren().add(row);
        return wrapper;
    }
}