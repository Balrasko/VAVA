package dev.vavateam1.dao;

import java.util.List;

import dev.vavateam1.model.InventoryIngredient;

public interface InventoryIngredientDao {
    List<InventoryIngredient> findAll();

    void saveAll(List<InventoryIngredient> ingredients);
}
