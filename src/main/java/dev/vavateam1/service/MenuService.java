package dev.vavateam1.service;

import dev.vavateam1.model.MenuItem;
import java.util.List;

public interface MenuService {
    List<MenuItem> getMenuItems();
    List<MenuItem> getMenuItemsByCategory(String category);
    void addMenuItem(MenuItem menuItem);
}