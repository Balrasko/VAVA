package dev.vavateam1.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.TableDao;
import dev.vavateam1.dto.KitchenOrder;
import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.OrderStatus;
import dev.vavateam1.model.Table;

public class KitchenService {

    private static final Set<OrderStatus> KITCHEN_BOARD_STATUSES = Set.of(
            OrderStatus.RECEIVED, OrderStatus.IN_PROGRESS, OrderStatus.DONE);

    private final OrderItemDao orderItemDao;
    private final MenuItemDao menuItemDao;
    private final TableDao tableDao;

    @Inject
    public KitchenService(OrderItemDao orderItemDao, MenuItemDao menuItemDao, TableDao tableDao) {
        this.orderItemDao = orderItemDao;
        this.menuItemDao = menuItemDao;
        this.tableDao = tableDao;
    }

    public List<KitchenOrder> getAllOrders() {
        Map<Integer, MenuItem> menuItemsById = menuItemDao.getAllMenuItems().stream()
                .collect(Collectors.toMap(MenuItem::getId, item -> item));
        Map<Integer, Integer> tableNumbersById = tableDao.findAll().stream()
                .collect(Collectors.toMap(Table::getId, Table::getTableNumber));

        Map<Integer, List<OrderItem>> itemsByTable = new LinkedHashMap<>();
        for (OrderItem item : orderItemDao.getUnpaidOrderItems()) {
            if (!isKitchenItem(item, menuItemsById)) {
                continue;
            }
            if (!KITCHEN_BOARD_STATUSES.contains(item.getStatus())) {
                continue;
            }
            itemsByTable.computeIfAbsent(item.getTableId(), key -> new ArrayList<>()).add(item);
        }

        return itemsByTable.entrySet().stream()
                .map(entry -> toKitchenOrder(entry.getKey(), entry.getValue(), menuItemsById, tableNumbersById))
                .sorted(Comparator.comparingInt(KitchenOrder::getId))
                .toList();
    }

    public void advanceOrderStatus(int orderId) {
        KitchenOrder order = findOrderById(orderId);

        if (order == null || order.getStatus() == OrderStatus.DONE) {
            return;
        }

        if (order.getStatus() == OrderStatus.RECEIVED) {
            for (OrderItemDto itemDto : order.getItems()) {
                OrderItem item = itemDto.getOrderItem();
                if (item.getStatus() == OrderStatus.RECEIVED) {
                    item.setStatus(OrderStatus.IN_PROGRESS);
                    orderItemDao.updateOrderItem(item);
                }
            }
            return;
        }

        for (OrderItemDto itemDto : order.getItems()) {
            OrderItem item = itemDto.getOrderItem();
            if (item.getStatus() != OrderStatus.DONE) {
                item.setStatus(OrderStatus.DONE);
                orderItemDao.updateOrderItem(item);
            }
        }
    }

    public void markOrderItemDone(int orderItemId) {
        OrderItem item = orderItemDao.getUnpaidOrderItems().stream()
                .filter(candidate -> candidate.getId() != null && candidate.getId() == orderItemId)
                .findFirst()
                .orElse(null);

        if (item == null || OrderStatus.DONE.equals(item.getStatus())) {
            return;
        }

        item.setStatus(OrderStatus.DONE);
        orderItemDao.updateOrderItem(item);
    }

    public void deleteDoneOrder(int orderId) {
        KitchenOrder order = findOrderById(orderId);

        if (order == null || order.getStatus() != OrderStatus.DONE) {
            return;
        }

        for (OrderItemDto itemDto : order.getItems()) {
            OrderItem item = itemDto.getOrderItem();
            item.setStatus(OrderStatus.SERVED);
            orderItemDao.updateOrderItem(item);
        }
    }

    private KitchenOrder findOrderById(int orderId) {
        return getAllOrders().stream()
                .filter(candidate -> candidate.getId() == orderId)
                .findFirst()
                .orElse(null);
    }

    private KitchenOrder toKitchenOrder(int tableId, List<OrderItem> items, Map<Integer, MenuItem> menuItemsById,
            Map<Integer, Integer> tableNumbersById) {
        List<OrderItemDto> itemDtos = items.stream()
                .map(item -> toOrderItemDto(item, menuItemsById))
                .toList();

        int tableNumber = tableNumbersById.getOrDefault(tableId, tableId);
        OrderStatus status = deriveOrderStatus(items);

        // For aggregated kitchen board orders, table id is a stable identifier for
        // actions.
        return new KitchenOrder(tableId, tableNumber, itemDtos, status);
    }

    private OrderItemDto toOrderItemDto(OrderItem item, Map<Integer, MenuItem> menuItemsById) {
        MenuItem menuItem = menuItemsById.get(item.getMenuItemId());
        if (menuItem == null) {
            MenuItem fallback = new MenuItem();
            fallback.setId(item.getMenuItemId());
            fallback.setName("Unknown item #" + item.getMenuItemId());
            menuItem = fallback;
        }
        return new OrderItemDto(new OrderItem(item), menuItem);
    }

    private OrderStatus deriveOrderStatus(List<OrderItem> items) {
        Set<OrderStatus> statuses = items.stream()
                .map(OrderItem::getStatus)
                .collect(Collectors.toSet());

    if (statuses.contains(OrderStatus.RECEIVED)) {
        return OrderStatus.RECEIVED;
    }
    if (statuses.contains(OrderStatus.IN_PROGRESS)) {
        return OrderStatus.IN_PROGRESS;
    }
        return OrderStatus.DONE;
    }

    private boolean isKitchenItem(OrderItem item, Map<Integer, MenuItem> menuItemsById) {
        MenuItem menuItem = menuItemsById.get(item.getMenuItemId());
        return menuItem != null && menuItem.isToKitchen();
    }

}
