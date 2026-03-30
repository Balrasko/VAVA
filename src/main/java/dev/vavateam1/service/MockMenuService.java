package dev.vavateam1.service;

import dev.vavateam1.model.MenuItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MockMenuService implements MenuService {

    private final List<MenuItem> menuItems = new ArrayList<>();

    public MockMenuService() {
        menuItems.add(new MenuItem(
                1,
                1, // food
                101,
                "Pizza Margherita",
                new BigDecimal("8.50"),
                true,
                "classic pizza with tomato and cheese",
                true,
                new BigDecimal("0.00"),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now()
        ));

        menuItems.add(new MenuItem(
                2,
                1, // food
                102,
                "Cheeseburger",
                new BigDecimal("9.90"),
                true,
                "beef burger with cheddar cheese",
                true,
                new BigDecimal("1.00"),
                LocalDateTime.now().minusDays(9),
                LocalDateTime.now()
        ));

        menuItems.add(new MenuItem(
                3,
                2, // drinks
                201,
                "Coca Cola",
                new BigDecimal("2.50"),
                true,
                "0.33l cold drink",
                false,
                new BigDecimal("0.00"),
                LocalDateTime.now().minusDays(8),
                LocalDateTime.now()
        ));

        menuItems.add(new MenuItem(
                4,
                2, // drinks
                202,
                "Espresso",
                new BigDecimal("1.90"),
                true,
                "small coffee",
                false,
                new BigDecimal("0.00"),
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        ));

        menuItems.add(new MenuItem(
                5,
                3, // services
                301,
                "Table Reservation",
                new BigDecimal("5.00"),
                true,
                "reservation service",
                false,
                new BigDecimal("0.00"),
                LocalDateTime.now().minusDays(6),
                LocalDateTime.now()
        ));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        return new ArrayList<>(menuItems);
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(String category) {
        int categoryId = mapCategoryToId(category);

        List<MenuItem> filteredItems = new ArrayList<>();
        for (MenuItem item : menuItems) {
            if (item.getCategoryId() == categoryId) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    @Override
    public void addMenuItem(MenuItem menuItem) {
        int nextId = menuItems.stream()
                .mapToInt(MenuItem::getId)
                .max()
                .orElse(0) + 1;

        menuItem.setId(nextId);
        menuItem.setCreatedAt(LocalDateTime.now());
        menuItem.setUpdatedAt(LocalDateTime.now());

        menuItems.add(menuItem);
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