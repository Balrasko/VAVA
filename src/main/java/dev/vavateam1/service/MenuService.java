package dev.vavateam1.service;

import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import java.util.List;

public interface MenuService {
    List<Category> getCategories();

    List<MenuItem> getMenuItems();

    List<MenuItem> getMenuItemsByCategoryId(int categoryId);

    void addMenuItem(MenuItem menuItem);
}