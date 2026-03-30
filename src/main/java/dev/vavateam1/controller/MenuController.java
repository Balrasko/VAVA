package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MenuController {

    @FXML private Button foodTabButton;
    @FXML private Button drinksTabButton;
    @FXML private Button servicesTabButton;

    @FXML private VBox foodSection;
    @FXML private VBox drinksSection;
    @FXML private VBox servicesSection;

    @FXML private VBox menuListPanel;
    @FXML private VBox menuFormPanel;

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField ingredientsField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField discountField;

    @FXML
    public void initialize() {
        setActiveTab("food");
        showSection("food");
        hideForm();
    }

    @FXML
    private void onFoodTab() {
        setActiveTab("food");
        showSection("food");
    }

    @FXML
    private void onDrinksTab() {
        setActiveTab("drinks");
        showSection("drinks");
    }

    @FXML
    private void onServicesTab() {
        setActiveTab("services");
        showSection("services");
    }

    @FXML
    private void onAddMenu() {
        clearForm();
        showForm();
    }

    @FXML
    private void onCloseForm() {
        hideForm();
    }

    @FXML
    private void onSubmitMenu() {
        String name = nameField.getText();
        String description = descriptionField.getText();
        String ingredients = ingredientsField.getText();
        String category = categoryField.getText();
        String price = priceField.getText();
        String discount = discountField.getText();

        // TODO backend:
        //ulozenie novej menu polozky

        hideForm();
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
        ingredientsField.clear();
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
}