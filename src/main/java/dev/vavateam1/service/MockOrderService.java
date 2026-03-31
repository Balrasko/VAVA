package dev.vavateam1.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import javafx.scene.layout.VBox;
import dev.vavateam1.model.Category;

public class MockOrderService {
    
    // All mock values made by AI

    public List<Category> getCategories() {
        // Get menu categories

        List<Category> categories = List.of(
            new Category(1, "Non-alcoholic drinks", LocalDateTime.now(), LocalDateTime.now()),
            new Category(2, "Alcoholic drinks", LocalDateTime.now(), LocalDateTime.now()),
            new Category(3, "Appetizers", LocalDateTime.now(), LocalDateTime.now()),
            new Category(4, "Main courses", LocalDateTime.now(), LocalDateTime.now()),
            new Category(5, "Desserts", LocalDateTime.now(), LocalDateTime.now())
        );

        return categories;
    }

    public List<MenuItem> getMenuItems() {

        // Get all menu items

        List<MenuItem> menuItems = List.of(
            // Non-Alcoholic Drinks
                new MenuItem(1, 1, 1001, "Coca-Cola", new BigDecimal("2.50"), true,
                        "Classic Coca-Cola soft drink", false, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(2, 1, 1002, "Orange Juice", new BigDecimal("3.00"), true,
                        "Fresh squeezed orange juice", false, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(3, 1, 1003, "Sparkling Water", new BigDecimal("2.00"), true,
                        "Carbonated mineral water", false, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                // Alcoholic Drinks
                new MenuItem(4, 2, 2001, "Draft Beer", new BigDecimal("4.50"), true,
                        "Local draft lager", false, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(5, 2, 2002, "House Red Wine", new BigDecimal("5.50"), true,
                        "Glass of house red wine", false, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(6, 2, 2003, "Mojito", new BigDecimal("7.00"), true,
                        "Rum cocktail with mint and lime", false, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),
                
                new MenuItem(61, 2, 2003, "Test1", new BigDecimal("7.00"), true,
                    "Rum cocktail with mint and lime", false, BigDecimal.ZERO,
                    LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(62, 2, 2003, "Test2", new BigDecimal("7.00"), true,
                    "Rum cocktail with mint and lime", false, BigDecimal.ZERO,
                    LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(63, 2, 2003, "Test3", new BigDecimal("7.00"), true,
                    "Rum cocktail with mint and lime", false, BigDecimal.ZERO,
                    LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(64, 2, 2003, "Test4", new BigDecimal("7.00"), true,
                    "Rum cocktail with mint and lime", false, BigDecimal.ZERO,
                    LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(65, 2, 2003, "Test5", new BigDecimal("7.00"), true,
                    "Rum cocktail with mint and lime", false, BigDecimal.ZERO,
                    LocalDateTime.now(), LocalDateTime.now()),

                // Appetizers
                new MenuItem(7, 3, 3001, "Garlic Bread", new BigDecimal("4.00"), true,
                        "Toasted bread with garlic butter", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(8, 3, 3002, "Bruschetta", new BigDecimal("5.50"), true,
                        "Grilled bread with tomato and basil", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(9, 3, 3003, "Fried Calamari", new BigDecimal("7.50"), true,
                        "Crispy fried squid with dipping sauce", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                // Main Courses
                new MenuItem(10, 4, 4001, "Grilled Chicken", new BigDecimal("12.50"), true,
                        "Grilled chicken breast with vegetables", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(11, 4, 4002, "Beef Burger", new BigDecimal("11.00"), true,
                        "Beef burger with fries", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(12, 4, 4003, "Spaghetti Carbonara", new BigDecimal("10.50"), true,
                        "Pasta with pancetta, egg and parmesan", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                // Desserts
                new MenuItem(13, 5, 5001, "Chocolate Cake", new BigDecimal("5.00"), true,
                        "Rich chocolate layered cake", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(14, 5, 5002, "Cheesecake", new BigDecimal("5.50"), true,
                        "Creamy New York style cheesecake", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now()),

                new MenuItem(15, 5, 5003, "Ice Cream Sundae", new BigDecimal("4.50"), true,
                        "Vanilla ice cream with chocolate syrup", true, BigDecimal.ZERO,
                        LocalDateTime.now(), LocalDateTime.now())
        );

        return menuItems;
    }

    public List<OrderItem> getOrderItems(Table table) {
        // Get order items for the selected table

        List<OrderItem> orderItems = List.of(
            new OrderItem(
                1,               // id
                1,       // menuItemId (Coca-Cola)
                null,     // paymentId (not paid yet)
                2,         // waiterId
                2,          // tableId
                2,         // quantity
                BigDecimal.ZERO,     // discount
                new BigDecimal("2.50"),
                "No ice",
                "PENDING",
                LocalDateTime.now(),
                LocalDateTime.now()
            ),

            new OrderItem(
                2,
                11,       // Beef Burger
                null,
                2,
                1,
                1,
                BigDecimal.ZERO,
                new BigDecimal("11.00"),
                "Medium rare",
                "IN_KITCHEN",
                LocalDateTime.now(),
                LocalDateTime.now()
            ),

            new OrderItem(
                3,
                7,      // Garlic Bread
                null,
                2,
                1,
                1,
                BigDecimal.ZERO,
                new BigDecimal("4.00"),
                "Extra garlic",
                "READY",
                LocalDateTime.now(),
                LocalDateTime.now()
            ),

            new OrderItem(
                4,
                13,        // Chocolate Cake
                null,
                2,
                1,
                2,
                BigDecimal.ZERO,
                new BigDecimal("5.00"),
                "One without cream",
                "PENDING",
                LocalDateTime.now(),
                LocalDateTime.now()
            )
        );

        List<OrderItem> results = orderItems.stream().filter(orderItem -> orderItem.getTableId() == table.getId()).toList();

        return results;
    }

    public OrderItem createOrderFromMenu(MenuItem menuItem) {
        OrderItem orderItem = new OrderItem();

        orderItem.setId(null);
        orderItem.setMenuItemId(menuItem.getId());
        orderItem.setPaymentId(null);
        orderItem.setWaiterId(null);
        orderItem.setQuantity(1);
        orderItem.setDiscount(BigDecimal.ZERO);
        orderItem.setPrice(menuItem.getPrice());
        orderItem.setNote(null);
        orderItem.setStatus("WAITING");
        orderItem.setCreatedAt(LocalDateTime.now());
        orderItem.setUpdatedAt(null);

        return orderItem;
    }

    public void saveOrder() {
        // Save the current state of the order
    }
}
