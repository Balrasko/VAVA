package dev.vavateam1.service;

import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;

public class MenuServiceImpl implements MenuService {

    private final CategoryDao categoryDao;
    private final MenuItemDao menuItemDao;

    @Inject
    public MenuServiceImpl(CategoryDao categoryDao, MenuItemDao menuItemDao) {
        this.categoryDao = categoryDao;
        this.menuItemDao = menuItemDao;
    }

    @Override
    public List<Category> getCategories() {
        return categoryDao.getAllCategories();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        return menuItemDao.getAllMenuItems();
    }

    @Override
    public List<MenuItem> getMenuItemsByCategoryId(int categoryId) {
        return menuItemDao.getMenuItemsByCategoryId(categoryId);
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        menuItemDao.addMenuItem(menuItem);
    }
}
