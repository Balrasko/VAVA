package dev.vavateam1.service;

import java.util.List;

import dev.vavateam1.model.KitchenOrder;

public interface KitchenOrderService {
    List<KitchenOrder> getAllOrders();

    void advanceOrderStatus(int orderId);

    void deleteDoneOrder(int orderId);
}
