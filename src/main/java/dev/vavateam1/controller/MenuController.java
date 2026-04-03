package dev.vavateam1.controller;

import com.google.inject.Inject;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.service.MenuService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuController {

    private final MenuService menuService;

    @FXML
    private HBox categoryTabsContainer;
    @FXML
    private VBox categorySectionsContainer;

    @FXML
    private VBox menuFormPanel;

    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField discountField;

    private final Map<Integer, Button> categoryButtons = new HashMap<>();
    private final Map<Integer, VBox> categorySections = new HashMap<>();
    private List<Category> categories = List.of();
    private int selectedCategoryId = -1;

    @Inject
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @FXML
    public void initialize() {
        loadCategories();
        hideForm();
    }

    @FXML
    private void onAddMenu() {
        clearForm();
        categoryField.setText(getSelectedCategoryName());
        showForm();
    }

    @FXML
    private void onCloseForm() {
        hideForm();
    }

    @FXML
    private void onSubmitMenu() {
        int categoryId = resolveCategoryId(categoryField.getText());
        if (categoryId <= 0) {
            return;
        }

        MenuItem newItem = new MenuItem(
                0,
                categoryId,
                generateItemCode(),
                nameField.getText(),
                new BigDecimal(priceField.getText().isBlank() ? "0.00" : priceField.getText()),
                true,
                descriptionField.getText(),
                isKitchenCategory(categoryField.getText()),
                new BigDecimal(discountField.getText().isBlank() ? "0.00" : discountField.getText()),
                LocalDateTime.now(),
                LocalDateTime.now());

        menuService.addMenuItem(newItem);
        hideForm();
        loadItems(selectedCategoryId);
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

    private void loadCategories() {
        categories = menuService.getCategories();
        categoryButtons.clear();
        categorySections.clear();

        categoryTabsContainer.getChildren().clear();
        categorySectionsContainer.getChildren().clear();

        for (Category category : categories) {
            int categoryId = category.getId();

            Button tabButton = new Button(category.getName());
            tabButton.getStyleClass().add("menu-tab");
            tabButton.setOnAction(e -> setActiveCategory(categoryId));
            categoryButtons.put(categoryId, tabButton);
            categoryTabsContainer.getChildren().add(tabButton);

            VBox section = new VBox(18);
            section.setVisible(false);
            section.setManaged(false);
            categorySections.put(categoryId, section);
            categorySectionsContainer.getChildren().add(section);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        categoryTabsContainer.getChildren().add(spacer);

        if (!categories.isEmpty()) {
            setActiveCategory(categories.get(0).getId());
        }
    }

    private void setActiveCategory(int categoryId) {
        selectedCategoryId = categoryId;
        setActiveTab(categoryId);
        showSection(categoryId);
        loadItems(categoryId);
    }

    private void setActiveTab(int activeCategoryId) {
        categoryButtons.forEach((categoryId, button) -> {
            button.getStyleClass().remove("menu-tab-active");
            if (categoryId == activeCategoryId) {
                button.getStyleClass().add("menu-tab-active");
            }
        });
    }

    private void showSection(int activeCategoryId) {
        categorySections.forEach((categoryId, section) -> {
            boolean visible = categoryId == activeCategoryId;
            section.setVisible(visible);
            section.setManaged(visible);
        });
    }

    private int resolveCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return selectedCategoryId;
        }

        String normalized = categoryName.trim();
        return categories.stream()
                .filter(category -> category.getName() != null && category.getName().equalsIgnoreCase(normalized))
                .map(Category::getId)
                .findFirst()
                .orElse(selectedCategoryId);
    }

    private String getSelectedCategoryName() {
        return categories.stream()
                .filter(category -> category.getId() == selectedCategoryId)
                .map(Category::getName)
                .findFirst()
                .orElse("");
    }

    private boolean isKitchenCategory(String category) {
        return category != null && category.toLowerCase().contains("food");
    }

    private int generateItemCode() {
        return (int) (100 + Math.random() * 900);
    }

    private void loadItems(int categoryId) {
        List<MenuItem> items = menuService.getMenuItemsByCategoryId(categoryId);
        renderItems(categoryId, items);
    }

    private void renderItems(int categoryId, List<MenuItem> items) {
        VBox targetSection = categorySections.get(categoryId);
        if (targetSection == null) {
            return;
        }

        targetSection.getChildren().clear();

        for (MenuItem item : items) {
            VBox itemBox = createMenuItemNode(item);
            targetSection.getChildren().add(itemBox);
        }
    }

    private VBox createMenuItemNode(MenuItem item) {
        VBox wrapper = new VBox(12);

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("menu-item-title");

        Label descriptionLabel = new Label(
                item.getDescription() != null ? item.getDescription() : "");
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