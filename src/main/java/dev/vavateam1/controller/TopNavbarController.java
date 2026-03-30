package dev.vavateam1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.util.function.Consumer;

public class TopNavbarController {

    @FXML private Button usersButton;
    @FXML private Button menuButton;
    @FXML private Button financesButton;
    @FXML private Button inventoryButton;
    @FXML private Button tableLayoutButton;

    private Consumer<String> tabSelectionHandler;

    @FXML
    private void onMenuToggle() {
        // TODO: open / collapse side menu
    }

    @FXML
    private void onUsers() {
        setActiveTab("users");
        notifyTabSelection("users");
    }

    @FXML
    private void onMenu() {
        setActiveTab("menu");
        notifyTabSelection("menu");
    }

    @FXML
    private void onFinances() {
        setActiveTab("finances");
        notifyTabSelection("finances");
    }

    @FXML
    private void onInventory() {
        setActiveTab("inventory");
        notifyTabSelection("inventory");
    }

    @FXML
    private void onTableLayout() {
        setActiveTab("tableLayout");
        notifyTabSelection("tableLayout");
    }

    public void setOnTabSelected(Consumer<String> tabSelectionHandler) {
        this.tabSelectionHandler = tabSelectionHandler;
    }
    private void notifyTabSelection(String tabName) {
        if (tabSelectionHandler != null) {
            tabSelectionHandler.accept(tabName);
        }
    }

    public void setActiveTab(String tabName) {
        usersButton.getStyleClass().remove("nav-tab-active");
        menuButton.getStyleClass().remove("nav-tab-active");
        financesButton.getStyleClass().remove("nav-tab-active");
        inventoryButton.getStyleClass().remove("nav-tab-active");
        tableLayoutButton.getStyleClass().remove("nav-tab-active");

        switch (tabName) {
            case "users" -> usersButton.getStyleClass().add("nav-tab-active");
            case "menu" -> menuButton.getStyleClass().add("nav-tab-active");
            case "finances" -> financesButton.getStyleClass().add("nav-tab-active");
            case "inventory" -> inventoryButton.getStyleClass().add("nav-tab-active");
            case "tableLayout" -> tableLayoutButton.getStyleClass().add("nav-tab-active");
        }
    }
}
