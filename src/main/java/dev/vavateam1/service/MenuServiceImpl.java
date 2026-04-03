package dev.vavateam1.service;

import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.model.MenuItem;

public class MenuServiceImpl implements MenuService {

    private final MenuItemDao menuItemDao;

    @Inject
    public MenuServiceImpl(MenuItemDao menuItemDao) {
        this.menuItemDao = menuItemDao;
    }

    @Override
    public List<MenuItem> getMenuItems() {
        return menuItemDao.getAllMenuItems();
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(String category) {
        int categoryId = mapCategoryToId(category);
        return menuItemDao.getMenuItemsByCategoryId(categoryId);
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        menuItemDao.addMenuItem(menuItem);
    }

    private int mapCategoryToId(String category) {
        return switch (category.toLowerCase()) {
            case "food" -> 1;
            case "drinks" -> 2;
            case "services" -> 3;
            default -> 0;
        };
    }
}
