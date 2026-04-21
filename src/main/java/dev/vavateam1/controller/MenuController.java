package dev.vavateam1.controller;

import com.google.inject.Inject;
import dev.vavateam1.dao.InventoryIngredientDao;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.service.MenuService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuController {

    private final MenuService menuService;
    private final InventoryIngredientDao inventoryIngredientDao;

    @FXML
    private HBox categoryTabsContainer;
    @FXML
    private VBox categorySectionsContainer;

    @FXML
    private ScrollPane menuFormPanel;
    @FXML
    private ScrollPane categoryFormPanel;

    @FXML
    private TextField nameField;
    @FXML
    private TextField descriptionField;
    @FXML
    private ComboBox<String> categoryField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField discountField;
    @FXML
    private MenuButton ingredientsButton;
    @FXML
    private TextField categoryNameField;

    @FXML
    private Button submitMenuButton;
    @FXML
    private Button deleteMenuButton;
    @FXML
    private Button submitCategoryButton;
    @FXML
    private Button deleteCategoryButton;
    @FXML
    private Button editCategoryButton;
    @FXML
    private Button deleteCategoryActionButton;
    @FXML
    private Label menuFormTitle;
    @FXML
    private Label categoryFormTitle;

    private final Map<Integer, Button> categoryButtons = new HashMap<>();
    private final Map<Integer, VBox> categorySections = new HashMap<>();
    private List<Category> categories = List.of();
    private int selectedCategoryId = -1;
    private int editingMenuItemId = -1;
    private int editingCategoryId = -1;

    @Inject
    public MenuController(MenuService menuService, InventoryIngredientDao inventoryIngredientDao) {
        this.menuService = menuService;
        this.inventoryIngredientDao = inventoryIngredientDao;
    }

    @FXML
    public void initialize() {
        loadIngredients();
        loadCategories();
        hideForm();
        hideCategoryForm();
    }

    @FXML
    private void onAddMenu() {
        clearForm();
        categoryField.setValue(getSelectedCategoryName());
        editingMenuItemId = -1;
        updateFormMode();
        hideCategoryForm();
        showForm();
    }

    @FXML
    private void onEditCategory() {
        if (selectedCategoryId <= 0) {
            return;
        }

        Category selectedCategory = categories.stream()
                .filter(category -> category.getId() == selectedCategoryId)
                .findFirst()
                .orElse(null);

        if (selectedCategory == null) {
            return;
        }

        editingCategoryId = selectedCategory.getId();
        categoryNameField.setText(selectedCategory.getName());
        updateCategoryFormMode();
        hideForm();
        showCategoryForm();
    }

    @FXML
    private void onCloseForm() {
        hideForm();
    }

    @FXML
    private void onCloseCategoryForm() {
        hideCategoryForm();
    }

    @FXML
    private void onSubmitMenu() {
        int categoryId = resolveCategoryId(categoryField.getValue());
        if (categoryId <= 0) {
            return;
        }

        MenuItem item = new MenuItem(
                editingMenuItemId > 0 ? editingMenuItemId : 0,
                categoryId,
                editingMenuItemId > 0 ? 0 : generateItemCode(),
                nameField.getText(),
                parseDecimal(priceField.getText()),
                true,
                descriptionField.getText(),
                isKitchenCategory(categoryField.getValue()),
                parseDecimal(discountField.getText()),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null);

        if (editingMenuItemId > 0) {
            menuService.updateMenuItem(item);
        } else {
            menuService.addMenuItem(item);
        }
        hideForm();
        loadItems(selectedCategoryId);
    }

    @FXML
    private void onDeleteMenu() {
        if (editingMenuItemId <= 0) {
            return;
        }

        menuService.softDeleteMenuItem(editingMenuItemId);
        hideForm();
        loadItems(selectedCategoryId);
    }

    @FXML
    private void onSubmitCategory() {
        String categoryName = categoryNameField.getText() != null ? categoryNameField.getText().trim() : "";
        if (categoryName.isEmpty()) {
            return;
        }

        if (editingCategoryId > 0) {
            menuService.updateCategory(editingCategoryId, categoryName);
        } else {
            Category newCategory = menuService.createCategory(categoryName);
            if (newCategory != null) {
                selectedCategoryId = newCategory.getId();
            }
        }

        hideCategoryForm();
        loadCategories();
    }

    @FXML
    private void onDeleteCategory() {
        if (editingCategoryId <= 0) {
            return;
        }

        int deletedCategoryId = editingCategoryId;
        menuService.softDeleteCategory(deletedCategoryId);
        hideCategoryForm();
        loadCategories();

        if (selectedCategoryId == deletedCategoryId && !categories.isEmpty()) {
            setActiveCategory(categories.get(0).getId());
        }
    }

    @FXML
    private void onDeleteCurrentCategory() {
        if (selectedCategoryId <= 0) {
            return;
        }

        menuService.softDeleteCategory(selectedCategoryId);
        hideCategoryForm();
        loadCategories();
    }

    private void showForm() {
        hideCategoryForm();
        menuFormPanel.setVisible(true);
        menuFormPanel.setManaged(true);
        menuFormPanel.setVvalue(0);
    }

    private void hideForm() {
        menuFormPanel.setVisible(false);
        menuFormPanel.setManaged(false);
        editingMenuItemId = -1;
        updateFormMode();
    }

    private void showCategoryForm() {
        categoryFormPanel.setVisible(true);
        categoryFormPanel.setManaged(true);
        categoryFormPanel.setVvalue(0);
    }

    private void hideCategoryForm() {
        categoryFormPanel.setVisible(false);
        categoryFormPanel.setManaged(false);
        editingCategoryId = -1;
        if (categoryNameField != null) {
            categoryNameField.clear();
        }
        updateCategoryFormMode();
    }

    private void loadIngredients() {
        if (ingredientsButton == null) {
            return;
        }
        ingredientsButton.getItems().clear();
        for (var ingredient : inventoryIngredientDao.findAll()) {
            CheckMenuItem item = new CheckMenuItem(ingredient.getName());
            item.selectedProperty().addListener((obs, oldVal, newVal) -> updateIngredientsButtonText());
            ingredientsButton.getItems().add(item);
        }
    }

    private void updateIngredientsButtonText() {
        List<String> selected = getSelectedIngredientNames();
        ingredientsButton.setText(selected.isEmpty() ? "Select ingredients..." : String.join(", ", selected));
    }

    private List<String> getSelectedIngredientNames() {
        return ingredientsButton.getItems().stream()
                .filter(item -> item instanceof CheckMenuItem)
                .map(item -> (CheckMenuItem) item)
                .filter(CheckMenuItem::isSelected)
                .map(CheckMenuItem::getText)
                .toList();
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        if (categoryField != null) {
            categoryField.getSelectionModel().clearSelection();
            categoryField.setValue(null);
        }
        priceField.clear();
        discountField.clear();
        if (ingredientsButton != null) {
            ingredientsButton.getItems().forEach(item -> {
                if (item instanceof CheckMenuItem cmi) cmi.setSelected(false);
            });
            updateIngredientsButtonText();
        }
    }

    private void updateFormMode() {
        boolean editing = editingMenuItemId > 0;

        if (submitMenuButton != null) {
            submitMenuButton.setText(editing ? "Save menu" : "Add menu");
        }

        if (deleteMenuButton != null) {
            deleteMenuButton.setVisible(editing);
            deleteMenuButton.setManaged(editing);
        }

        if (menuFormTitle != null) {
            menuFormTitle.setText(editing ? "Edit menu" : "Add menu");
        }
    }

    private void updateCategoryFormMode() {
        boolean editing = editingCategoryId > 0;

        if (submitCategoryButton != null) {
            submitCategoryButton.setText(editing ? "Save category" : "Add category");
        }

        if (deleteCategoryButton != null) {
            deleteCategoryButton.setVisible(editing);
            deleteCategoryButton.setManaged(editing);
        }

        if (categoryFormTitle != null) {
            categoryFormTitle.setText(editing ? "Edit category" : "Add category");
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return new BigDecimal("0.00");
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return new BigDecimal("0.00");
        }
    }

    private void openEditForm(MenuItem item) {
        editingMenuItemId = item.getId();
        nameField.setText(item.getName());
        descriptionField.setText(item.getDescription());
        categoryField.setValue(resolveCategoryName(item.getCategoryId()));
        priceField.setText(item.getPrice() != null ? item.getPrice().toString() : "");
        discountField.setText(item.getDiscount() != null ? item.getDiscount().toString() : "");
        updateFormMode();
        showForm();
    }

    private String resolveCategoryName(int categoryId) {
        return categories.stream()
                .filter(category -> category.getId() == categoryId)
                .map(Category::getName)
                .findFirst()
                .orElse("");
    }

    private void loadCategories() {
        categories = menuService.getCategories();
        refreshCategoryDropdownOptions();
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

        Button addCategoryTabButton = new Button("+");
        addCategoryTabButton.getStyleClass().addAll("menu-tab", "menu-tab-add");
        addCategoryTabButton.setOnAction(e -> {
            editingCategoryId = -1;
            categoryNameField.clear();
            updateCategoryFormMode();
            hideForm();
            showCategoryForm();
        });
        categoryTabsContainer.getChildren().add(addCategoryTabButton);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        categoryTabsContainer.getChildren().add(spacer);

        if (!categories.isEmpty()) {
            setActiveCategory(categories.get(0).getId());
        } else {
            selectedCategoryId = -1;
        }

        if (editCategoryButton != null) {
            editCategoryButton.setDisable(categories.isEmpty() || selectedCategoryId <= 0);
        }
        if (deleteCategoryActionButton != null) {
            deleteCategoryActionButton.setDisable(categories.isEmpty() || selectedCategoryId <= 0);
        }
    }

    private void refreshCategoryDropdownOptions() {
        if (categoryField == null) {
            return;
        }

        String currentValue = categoryField.getValue();
        List<String> categoryNames = categories.stream()
                .map(Category::getName)
                .toList();

        categoryField.getItems().setAll(categoryNames);
        if (currentValue != null && categoryNames.stream().anyMatch(name -> name.equalsIgnoreCase(currentValue))) {
            categoryField.setValue(currentValue);
        }
    }

    private void setActiveCategory(int categoryId) {
        selectedCategoryId = categoryId;
        setActiveTab(categoryId);
        showSection(categoryId);
        loadItems(categoryId);

        if (editCategoryButton != null) {
            editCategoryButton.setDisable(selectedCategoryId <= 0);
        }
        if (deleteCategoryActionButton != null) {
            deleteCategoryActionButton.setDisable(selectedCategoryId <= 0);
        }
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
        wrapper.getStyleClass().add("menu-item-card");

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("menu-item-title");
        nameLabel.setWrapText(true);

        Label descriptionLabel = new Label(
                item.getDescription() != null ? item.getDescription() : "");
        descriptionLabel.getStyleClass().add("menu-item-description");
        descriptionLabel.setWrapText(true);

        VBox textBox = new VBox(4, nameLabel, descriptionLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        textBox.setMaxWidth(Double.MAX_VALUE);

        Label priceLabel = new Label(item.getPrice() != null ? item.getPrice().toString() + " €" : "");
        priceLabel.getStyleClass().add("menu-action-label");

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("secondary-action-button");
        editButton.setOnAction(e -> openEditForm(item));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("secondary-action-button");
        deleteButton.setOnAction(e -> {
            menuService.softDeleteMenuItem(item.getId());
            loadItems(selectedCategoryId);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsRow = new HBox(8, priceLabel, spacer, editButton, deleteButton);
        actionsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionsRow.setMaxWidth(Double.MAX_VALUE);

        wrapper.getChildren().addAll(textBox, actionsRow);
        return wrapper;
    }
}
