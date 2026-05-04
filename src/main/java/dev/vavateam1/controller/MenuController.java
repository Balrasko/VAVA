package dev.vavateam1.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.service.InventoryService;
import dev.vavateam1.service.MenuService;
import dev.vavateam1.util.I18n;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MenuController {

    private final MenuService menuService;
    private final InventoryService inventoryService;

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
    private TextField pluCodeField;
    @FXML
    private CheckBox isKitchenCheckBox;
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
    private Label menuFormTitle;
    @FXML
    private Label categoryFormTitle;

    private final Map<Integer, Button> categoryButtons = new HashMap<>();
    private final Map<Integer, VBox> categorySections = new HashMap<>();
    private List<Category> categories = List.of();
    private int selectedCategoryId = -1;
    private int editingMenuItemId = -1;
    private int editingCategoryId = -1;
    private Integer editingMenuItemCode;

    @Inject
    public MenuController(MenuService menuService, InventoryService inventoryService) {
        this.menuService = menuService;
        this.inventoryService = inventoryService;
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
        if (!validateMenuForm()) {
            return;
        }

        int categoryId = resolveCategoryId(categoryField.getValue());
        int itemCode = resolveMenuItemCode();

        MenuItem item = new MenuItem(
                editingMenuItemId > 0 ? editingMenuItemId : 0,
                categoryId,
                itemCode,
                nameField.getText(),
                parseDecimal(priceField.getText()),
                true,
                descriptionField.getText(),
                isKitchenCheckBox.isSelected(),
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

    private boolean validateMenuForm() {
        boolean valid = true;

        if (nameField.getText() == null || nameField.getText().isBlank()) {
            setFieldError(nameField, true);
            valid = false;
        } else {
            setFieldError(nameField, false);
        }

        if (categoryField.getValue() == null || categoryField.getValue().isBlank()) {
            categoryField.getStyleClass().add("form-input-error");
            valid = false;
        } else {
            categoryField.getStyleClass().remove("form-input-error");
        }

        String priceText = priceField.getText();
        if (priceText == null || priceText.isBlank()) {
            setFieldError(priceField, true);
            valid = false;
        } else {
            try {
                BigDecimal price = new BigDecimal(priceText.trim());
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    setFieldError(priceField, true);
                    valid = false;
                } else {
                    setFieldError(priceField, false);
                }
            } catch (NumberFormatException e) {
                setFieldError(priceField, true);
                valid = false;
            }
        }

        String discountText = discountField.getText();
        if (discountText != null && !discountText.isBlank()) {
            try {
                BigDecimal discount = new BigDecimal(discountText.trim());
                if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal("100")) > 0) {
                    setFieldError(discountField, true);
                    valid = false;
                } else {
                    setFieldError(discountField, false);
                }
            } catch (NumberFormatException e) {
                setFieldError(discountField, true);
                valid = false;
            }
        } else {
            setFieldError(discountField, false);
        }

        String pluCodeText = pluCodeField.getText() != null ? pluCodeField.getText().trim() : "";
        if (!pluCodeText.isEmpty()) {
            try {
                int pluCode = Integer.parseInt(pluCodeText);
                boolean duplicatePluCode = menuService.getMenuItems().stream()
                        .anyMatch(item -> item.getItemCode() == pluCode && item.getId() != editingMenuItemId);
                if (pluCode <= 0 || duplicatePluCode) {
                    setFieldError(pluCodeField, true);
                    valid = false;
                } else {
                    setFieldError(pluCodeField, false);
                }
            } catch (NumberFormatException e) {
                setFieldError(pluCodeField, true);
                valid = false;
            }
        } else {
            setFieldError(pluCodeField, false);
        }

        return valid;
    }

    private void setFieldError(TextField field, boolean error) {
        if (error) {
            if (!field.getStyleClass().contains("form-input-error")) {
                field.getStyleClass().add("form-input-error");
            }
            field.textProperty().addListener((obs, oldVal, newVal) -> setFieldError(field, false));
        } else {
            field.getStyleClass().remove("form-input-error");
        }
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
        editingMenuItemCode = null;
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
        for (var ingredient : inventoryService.getAll()) {
            CheckBox checkBox = new CheckBox(ingredient.getName());
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateIngredientsButtonText());
            CustomMenuItem item = new CustomMenuItem(checkBox, false);
            ingredientsButton.getItems().add(item);
        }
    }

    private void updateIngredientsButtonText() {
        List<String> selected = getSelectedIngredientNames();
        ingredientsButton.setText(selected.isEmpty() ? I18n.t("menu.selectIngredients") : String.join(", ", selected));
    }

    private List<String> getSelectedIngredientNames() {
        return ingredientsButton.getItems().stream()
                .filter(item -> item instanceof CustomMenuItem)
                .map(item -> (CustomMenuItem) item)
                .filter(item -> item.getContent() instanceof CheckBox && ((CheckBox) item.getContent()).isSelected())
                .map(item -> ((CheckBox) item.getContent()).getText())
                .toList();
    }

    private void clearForm() {
        nameField.clear();
        descriptionField.clear();
        pluCodeField.clear();
        if (categoryField != null) {
            categoryField.getSelectionModel().clearSelection();
            categoryField.setValue(null);
        }
        priceField.clear();
        discountField.clear();
        if (isKitchenCheckBox != null) {
            isKitchenCheckBox.setSelected(false);
        }
        if (ingredientsButton != null) {
            ingredientsButton.getItems().forEach(item -> {
                if (item instanceof CustomMenuItem cmi && cmi.getContent() instanceof CheckBox cb)
                    cb.setSelected(false);
            });
            updateIngredientsButtonText();
        }

        editingMenuItemCode = null;
    }

    private void updateFormMode() {
        boolean editing = editingMenuItemId > 0;

        if (submitMenuButton != null) {
            submitMenuButton.setText(editing ? I18n.t("menu.saveItem") : I18n.t("menu.addItem"));
        }

        if (deleteMenuButton != null) {
            deleteMenuButton.setVisible(editing);
            deleteMenuButton.setManaged(editing);
        }

        if (menuFormTitle != null) {
            menuFormTitle.setText(editing ? I18n.t("menu.editMenuItem") : I18n.t("menu.addMenuItem"));
        }
    }

    private void updateCategoryFormMode() {
        boolean editing = editingCategoryId > 0;

        if (submitCategoryButton != null) {
            submitCategoryButton.setText(editing ? I18n.t("menu.saveCategory") : I18n.t("menu.addCategory"));
        }

        if (deleteCategoryButton != null) {
            deleteCategoryButton.setVisible(editing);
            deleteCategoryButton.setManaged(editing);
        }

        if (categoryFormTitle != null) {
            categoryFormTitle.setText(editing ? I18n.t("menu.editCategory") : I18n.t("menu.addCategory"));
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
        editingMenuItemCode = item.getItemCode();
        nameField.setText(item.getName());
        descriptionField.setText(item.getDescription());
        pluCodeField.setText(item.getItemCode() > 0 ? String.valueOf(item.getItemCode()) : "");
        categoryField.setValue(resolveCategoryName(item.getCategoryId()));
        priceField.setText(item.getPrice() != null ? item.getPrice().toString() : "");
        discountField.setText(item.getDiscount() != null ? item.getDiscount().toString() : "");
        isKitchenCheckBox.setSelected(item.isToKitchen());
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

    private int resolveMenuItemCode() {
        String pluCodeText = pluCodeField.getText() != null ? pluCodeField.getText().trim() : "";
        if (!pluCodeText.isEmpty()) {
            return Integer.parseInt(pluCodeText);
        }
        if (editingMenuItemCode != null && editingMenuItemCode > 0) {
            return editingMenuItemCode;
        }
        return generateItemCode();
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
        wrapper.setMaxWidth(Double.MAX_VALUE);

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

        Button editButton = new Button(I18n.t("common.edit"));
        editButton.getStyleClass().add("secondary-action-button");
        editButton.setOnAction(e -> openEditForm(item));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsRow = new HBox(8, priceLabel, spacer, editButton);
        actionsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionsRow.setMaxWidth(Double.MAX_VALUE);

        wrapper.getChildren().addAll(textBox, actionsRow);
        return wrapper;
    }
}
