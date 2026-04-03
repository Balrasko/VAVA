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
    private Runnable addZoneRequestHandler;
    private boolean addZoneTabVisible = true;

    private Map<Integer, Button> zoneButtons = new HashMap<>();

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

                zonesContainer.getChildren().add(node);
            }

            if (addZoneTabVisible) {
                Button addZoneTabButton = new Button("+");
                addZoneTabButton.getStyleClass().addAll("nav-tab", "zone-nav-button", "zone-nav-button-add");
                addZoneTabButton.setOnAction(e -> {
                    if (addZoneRequestHandler != null) {
                        addZoneRequestHandler.run();
                    }
                });
                zonesContainer.getChildren().add(addZoneTabButton);
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

    public void setOnAddZoneRequested(Runnable addZoneRequestHandler) {
        this.addZoneRequestHandler = addZoneRequestHandler;
    }

    public void setAddZoneTabVisible(boolean visible) {
        this.addZoneTabVisible = visible;
        loadZones();
    }

    public void setActiveZone(int zoneId) {
        zoneButtons.values().forEach(btn -> btn.getStyleClass().remove("nav-tab-active"));
        if (zoneButtons.containsKey(zoneId)) {
            zoneButtons.get(zoneId).getStyleClass().add("nav-tab-active");
        }
    }

    private void notifyZoneSelection(int zoneId) {
        if (zoneSelectionHandler != null) {
            zoneSelectionHandler.accept(zoneId);
        }
    }
}
