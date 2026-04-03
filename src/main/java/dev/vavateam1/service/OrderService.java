package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.List;

import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import dev.vavateam1.dto.OrderItemView;

public interface OrderService {
    List<Category> getCategories();

    List<MenuItem> getMenuItems();

    List<OrderItem> getOrderItems(Table table);

    OrderItem createOrderFromMenu(MenuItem menuItem);

    List<MenuItem> getItemsByPluCode(String code);

    void saveTempOrders(List<OrderItemView> orderItemList);

    void processPayment(List<OrderItem> ordersToProcess, int paymentMethod, BigDecimal totalPrice, BigDecimal tip);
}
