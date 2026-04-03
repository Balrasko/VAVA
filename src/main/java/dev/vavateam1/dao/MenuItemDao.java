package dev.vavateam1.dao;

import dev.vavateam1.model.MenuItem;
import java.util.List;

public interface MenuItemDao {
    List<MenuItem> getAllMenuItems();

    List<MenuItem> getMenuItemsByCategoryId(int categoryId);

    void addMenuItem(MenuItem menuItem);
}
