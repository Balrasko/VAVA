package dev.vavateam1.dao;

import java.util.List;
import dev.vavateam1.model.Category;

public interface CategoryDao {
    List<Category> getAllCategories();

    Category createCategory(String name);

    void updateCategory(int categoryId, String name);

    void softDeleteCategory(int categoryId);
}
