package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;

import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dto.CreateOrder;
import dev.vavateam1.dto.OrderItemDto;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.OrderStatus;
import dev.vavateam1.model.Table;

public class OrderServiceImpl implements OrderService {

    private final MenuItemDao menuItemDao;
    private final OrderItemDao orderItemDao;
    private final CategoryDao categoryDao;
    private final PaymentDao paymentDao;
    private final AuthService authService;

    @Inject
    public OrderServiceImpl(MenuItemDao menuItemDao, OrderItemDao orderItemDao, CategoryDao categoryDao,
            PaymentDao paymentDao, AuthService authService) {
        this.menuItemDao = menuItemDao;
        this.orderItemDao = orderItemDao;
        this.categoryDao = categoryDao;
        this.paymentDao = paymentDao;
        this.authService = authService;
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
    public List<OrderItem> getOrderItems(Table table) {
        return orderItemDao.getOrderItemsByTableId(table.getId());
    }

    @Override
    public OrderItem createOrderFromMenu(MenuItem menuItem, Table table) {
        if (table == null) {
            throw new IllegalArgumentException("Table is required to create an order item.");
        }

        int waiterId = 0;
        if (authService != null && authService.getUser() != null) {
            waiterId = authService.getUser().getId();
        }

        CreateOrder createDto = new CreateOrder(
                menuItem.getId(),
                null,
                waiterId,
                table.getId(),
                1,
                BigDecimal.ZERO,
                menuItem.getPrice(),
                null,
                OrderStatus.RECEIVED);

        return orderItemDao.createOrderItem(createDto);
    }

    @Override
    public List<MenuItem> getItemsByPluCode(String code) {
        try {
            int pluCode = Integer.parseInt(code);
            return menuItemDao.getItemsByPluCode(pluCode);
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void saveTempOrders(List<OrderItemDto> orderItemList) {
        for (OrderItemDto view : orderItemList) {
            OrderItem item = view.getOrderItem();
            if (item.getId() != null) {
                orderItemDao.updateOrderItem(item);
            } else {
                OrderItem createdItem = orderItemDao.createOrderItem(toCreateDto(item));
                item.setId(createdItem.getId());
                item.setCreatedAt(createdItem.getCreatedAt());
                item.setUpdatedAt(createdItem.getUpdatedAt());
            }
        }
    }

    @Override
    public void deleteOrderItem(int orderItemId) {
        orderItemDao.deleteOrderItem(orderItemId);
    }

    @Override
    public void processPayment(List<OrderItem> ordersToProcess, int paymentMethod, BigDecimal totalPrice,
            BigDecimal tip) {
        if (ordersToProcess == null || ordersToProcess.isEmpty()) {
            return;
        }

        int waiterId = ordersToProcess.get(0).getWaiterId();

        int paymentId = paymentDao.createPayment(waiterId, paymentMethod, totalPrice, false, tip);

        for (OrderItem item : ordersToProcess) {
            item.setPaymentId(paymentId);
            if (item.getId() != null) {
                orderItemDao.updateOrderItem(item);
            } else {
                OrderItem createdItem = orderItemDao.createOrderItem(toCreateDto(item));
                item.setId(createdItem.getId());
                item.setCreatedAt(createdItem.getCreatedAt());
                item.setUpdatedAt(createdItem.getUpdatedAt());
            }
        }
    }

    private CreateOrder toCreateDto(OrderItem item) {
        return new CreateOrder(
                item.getMenuItemId(),
                item.getPaymentId(),
                item.getWaiterId(),
                item.getTableId(),
                item.getQuantity(),
                item.getDiscount(),
                item.getPrice(),
                item.getNote(),
                item.getStatus());
    }

    private String resolveInitialStatus(MenuItem menuItem, Table table) {
        if (!menuItem.isToKitchen()) {
            return "WAITING";
        }

        boolean hasActiveKitchenItems = orderItemDao.hasActiveKitchenItemsByTableId(table.getId());
        return hasActiveKitchenItems ? "IN_PROGRESS" : "WAITING";
    }
}
