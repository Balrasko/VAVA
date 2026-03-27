package dev.vavateam1.service;

import java.util.ArrayList;
import java.util.List;

import dev.vavateam1.model.KitchenOrder;
import dev.vavateam1.model.KitchenOrderItem;
import dev.vavateam1.model.OrderStatus;

public class MockKitchenOrderService implements KitchenOrderService {

    private final List<KitchenOrder> orders = new ArrayList<>();

    public MockKitchenOrderService() {
        orders.add(new KitchenOrder(
                101,
                3,
                new ArrayList<>(List.of(
                        new KitchenOrderItem("Burger", 2, "Bez cibule"),
                        new KitchenOrderItem("Hranolky", 1, null),
                        new KitchenOrderItem("Coleslaw", 1, null))),
                OrderStatus.RECEIVED));

        orders.add(new KitchenOrder(
                102,
                7,
                new ArrayList<>(List.of(
                        new KitchenOrderItem("Caesar salad", 1, "Extra dressing"),
                        new KitchenOrderItem("Lemonade", 2, null))),
                OrderStatus.IN_PROGRESS));

        orders.add(new KitchenOrder(
                103,
                12,
                new ArrayList<>(List.of(
                        new KitchenOrderItem("Pizza Margherita", 1, null),
                        new KitchenOrderItem("Pizza Prosciutto", 1, "Bez oliv"),
                        new KitchenOrderItem("Mineralka", 3, "Jemne perliva"),
                        new KitchenOrderItem("Tiramisu", 2, null))),
                OrderStatus.DONE));

        orders.add(new KitchenOrder(
                104,
                5,
                new ArrayList<>(List.of(
                        new KitchenOrderItem("Steak", 2, "Medium rare"),
                        new KitchenOrderItem("Grilovana zelenina", 2, null))),
                OrderStatus.RECEIVED));

        orders.add(new KitchenOrder(
                105,
                9,
                new ArrayList<>(List.of(
                        new KitchenOrderItem("Polievka dňa", 3, null),
                        new KitchenOrderItem("Cesnakový chlieb", 2, null),
                        new KitchenOrderItem("Espresso", 3, null))),
                OrderStatus.IN_PROGRESS));
    }

    @Override
    public List<KitchenOrder> getAllOrders() {
        return orders.stream()
                .map(this::copyOrder)
                .toList();
    }

    @Override
    public void advanceOrderStatus(int orderId) {
        KitchenOrder order = findOrder(orderId);
        if (order == null || order.getStatus() == OrderStatus.DONE) {
            return;
        }

        if (order.getStatus() == OrderStatus.RECEIVED) {
            order.setStatus(OrderStatus.IN_PROGRESS);
            return;
        }

        order.setStatus(OrderStatus.DONE);
    }

    @Override
    public void deleteDoneOrder(int orderId) {
        orders.removeIf(order -> order.getId() == orderId && order.getStatus() == OrderStatus.DONE);
    }

    private KitchenOrder findOrder(int orderId) {
        return orders.stream()
                .filter(order -> order.getId() == orderId)
                .findFirst()
                .orElse(null);
    }

    private KitchenOrder copyOrder(KitchenOrder source) {
        List<KitchenOrderItem> copiedItems = source.getItems().stream()
                .map(item -> new KitchenOrderItem(item.getName(), item.getQuantity(), item.getNote()))
                .toList();

        return new KitchenOrder(
                source.getId(),
                source.getTableNumber(),
                new ArrayList<>(copiedItems),
                source.getStatus());
    }
}
