package dev.vavateam1.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;

public class MenuServiceImpl implements MenuService {
    private static final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final CategoryDao categoryDao;
    private final MenuItemDao menuItemDao;

    @Inject
    public MenuServiceImpl(CategoryDao categoryDao, MenuItemDao menuItemDao) {
        this.categoryDao = categoryDao;
        this.menuItemDao = menuItemDao;
    }

    @Override
    public List<Category> getCategories() {
        log.info("Fetching all categories");
        return categoryDao.getAllCategories();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        log.info("Fetching all menu items");
        return menuItemDao.getAllMenuItems();
    }

    @Override
    public List<MenuItem> getMenuItemsByCategoryId(int categoryId) {
        log.info("Fetching menu items for category id: {}", categoryId);
        return menuItemDao.getMenuItemsByCategoryId(categoryId);
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        log.info("Adding menu item: {}", menuItem.getName());
        menuItemDao.addMenuItem(menuItem);
        log.info("Menu item added: {}", menuItem.getName());
    }

    @Override
    public void updateMenuItem(MenuItem menuItem) {
        log.info("Updating menu item id: {}", menuItem.getId());
        menuItemDao.updateMenuItem(menuItem);
        log.info("Menu item updated id: {}", menuItem.getId());
    }

    @Override
    public void softDeleteMenuItem(int menuItemId) {
        log.info("Soft deleting menu item id: {}", menuItemId);
        menuItemDao.softDeleteMenuItem(menuItemId);
        log.info("Menu item soft deleted id: {}", menuItemId);
    }

    @Override
    public Category createCategory(String name) {
        log.info("Creating category: {}", name);
        Category created = categoryDao.createCategory(name);
        log.info("Category created with id: {}", created.getId());
        return created;
    }

    @Override
    public void updateCategory(int categoryId, String name) {
        log.info("Updating category id: {} to name: {}", categoryId, name);
        categoryDao.updateCategory(categoryId, name);
        log.info("Category updated id: {}", categoryId);
    }

    @Override
    public void softDeleteCategory(int categoryId) {
        log.info("Soft deleting category id: {}", categoryId);
        categoryDao.softDeleteCategory(categoryId);
        log.info("Category soft deleted id: {}", categoryId);
    }

    @Override
    public MenuItem getItemByPluCode(int pluCode) {
        return menuItemDao.getItemByPluCode(pluCode);
    }

}
