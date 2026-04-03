package dev.vavateam1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import com.google.inject.Inject;

import dev.vavateam1.model.Location;
import dev.vavateam1.service.TableService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class TopNavbarZonesController {

    @FXML
    private HBox zonesContainer;

    private final TableService tableService;

    private IntConsumer zoneSelectionHandler;
    private boolean editMode = false;

    private Map<Integer, Button> zoneButtons = new HashMap<>();
    private Map<Integer, Button> deleteButtons = new HashMap<>();

    @Inject
    public TopNavbarZonesController(TableService tableService) {
        this.tableService = tableService;
    }

    @FXML
    public void initialize() {
        loadZones();
    }

    public void loadZones() {
        List<Location> locations = tableService.getLocations();
        zonesContainer.getChildren().clear();
        zoneButtons.clear();
        deleteButtons.clear();

        try {
            for (Location location : locations) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/top-navbar-zone-item.fxml"));
                Node node = loader.load();

                Button zoneButton = (Button) node.lookup("#zoneButton");
                zoneButton.setText(location.getName());
                zoneButton.setOnAction(e -> {
                    setActiveZone(location.getId());
                    notifyZoneSelection(location.getId());
                });
                zoneButtons.put(location.getId(), zoneButton);

                Button deleteButton = (Button) node.lookup("#deleteButton");
                deleteButton.setVisible(editMode);
                deleteButton.setManaged(editMode);
                deleteButton.setOnAction(e -> {
                    if (!editMode)
                        return;
                    System.out.println("Delete zone: " + location.getName());
                });
                deleteButtons.put(location.getId(), deleteButton);

                zonesContainer.getChildren().add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        zonesContainer.getChildren().add(spacer);
    }

    public void setOnZoneSelected(IntConsumer zoneSelectionHandler) {
        this.zoneSelectionHandler = zoneSelectionHandler;
    }

    public void setEditMode(boolean enabled) {
        editMode = enabled;
        setDeleteButtonsVisible(enabled);
    }

    public void setActiveZone(int zoneId) {
        zoneButtons.values().forEach(btn -> btn.getStyleClass().remove("nav-tab-active"));
        if (zoneButtons.containsKey(zoneId)) {
            zoneButtons.get(zoneId).getStyleClass().add("nav-tab-active");
        }
    }

    private void setDeleteButtonsVisible(boolean visible) {
        deleteButtons.values().forEach(btn -> {
            btn.setVisible(visible);
            btn.setManaged(visible);
        });
    }

    private void notifyZoneSelection(int zoneId) {
        if (zoneSelectionHandler != null) {
            zoneSelectionHandler.accept(zoneId);
        }
    }
}
