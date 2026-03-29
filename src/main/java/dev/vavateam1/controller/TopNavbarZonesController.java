package dev.vavateam1.controller;

import java.util.function.IntConsumer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TopNavbarZonesController {

    @FXML
    private Button restaurantButton;

    @FXML
    private Button teraceButton;

    @FXML
    private Button barButton;

    @FXML
    private Button restaurantDeleteButton;

    @FXML
    private Button teraceDeleteButton;

    @FXML
    private Button barDeleteButton;

    private IntConsumer zoneSelectionHandler;
    private boolean editMode = false;

    @FXML
    private void onRestaurant() {
        setActiveZone(1);
        notifyZoneSelection(1);
    }

    @FXML
    private void onTerace() {
        setActiveZone(2);
        notifyZoneSelection(2);
    }

    @FXML
    private void onBar() {
        setActiveZone(3);
        notifyZoneSelection(3);
    }

    public void setOnZoneSelected(IntConsumer zoneSelectionHandler) {
        this.zoneSelectionHandler = zoneSelectionHandler;
    }

    public void setEditMode(boolean enabled) {
        editMode = enabled;
        setDeleteButtonsVisible(enabled);
    }

    public void setActiveZone(int zoneId) {
        restaurantButton.getStyleClass().remove("nav-tab-active");
        teraceButton.getStyleClass().remove("nav-tab-active");
        barButton.getStyleClass().remove("nav-tab-active");

        switch (zoneId) {
            case 1 -> restaurantButton.getStyleClass().add("nav-tab-active");
            case 2 -> teraceButton.getStyleClass().add("nav-tab-active");
            case 3 -> barButton.getStyleClass().add("nav-tab-active");
            default -> {
            }
        }
    }

    @FXML
    private void onDeleteRestaurant() {
        if (!editMode) {
            return;
        }
        System.out.println("Delete zone Restaurant");
    }

    @FXML
    private void onDeleteTerace() {
        if (!editMode) {
            return;
        }
        System.out.println("Delete zone Terace");
    }

    @FXML
    private void onDeleteBar() {
        if (!editMode) {
            return;
        }
        System.out.println("Delete zone Bar");
    }

    private void setDeleteButtonsVisible(boolean visible) {
        if (restaurantDeleteButton != null) {
            restaurantDeleteButton.setVisible(visible);
            restaurantDeleteButton.setManaged(visible);
        }
        if (teraceDeleteButton != null) {
            teraceDeleteButton.setVisible(visible);
            teraceDeleteButton.setManaged(visible);
        }
        if (barDeleteButton != null) {
            barDeleteButton.setVisible(visible);
            barDeleteButton.setManaged(visible);
        }
    }

    private void notifyZoneSelection(int zoneId) {
        if (zoneSelectionHandler != null) {
            zoneSelectionHandler.accept(zoneId);
        }
    }
}