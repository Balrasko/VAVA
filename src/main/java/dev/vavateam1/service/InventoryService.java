package dev.vavateam1.service;

import dev.vavateam1.model.InventoryIngredient;
import dev.vavateam1.model.InventoryItemStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InventoryService {
    List<InventoryIngredient> getAll();
    void saveAll(List<InventoryIngredient> items);
    void delete(int id);
    InventoryItemStatus getStatus(InventoryIngredient item);
    List<InventoryIngredient> importFromXml(Path path) throws IOException;
    void exportToXml(Path path, List<InventoryIngredient> items) throws IOException;
}
