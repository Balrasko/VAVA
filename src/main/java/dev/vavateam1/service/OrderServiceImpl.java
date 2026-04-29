package dev.vavateam1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

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
    public List<MenuItem> getMenuItemsIncludingDeleted() {
        return menuItemDao.getAllMenuItemsIncludingDeleted();
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

        log.info("Creating order item for menu item '{}' (id: {}) on table id: {}", menuItem.getName(), menuItem.getId(), table.getId());
        BigDecimal itemDiscount = normalizeDiscount(menuItem.getDiscount());
        BigDecimal discountedPrice = applyDiscount(menuItem.getPrice(), itemDiscount);

        CreateOrder createDto = new CreateOrder(
                menuItem.getId(),
                null,
                waiterId,
                table.getId(),
                1,
                itemDiscount,
                discountedPrice,
                null,
                OrderStatus.RECEIVED);

        OrderItem created = orderItemDao.createOrderItem(createDto);
        log.info("Order item created with id: {}", created.getId());
        return created;
    }

    @Override
    public MenuItem getItemByPluCode(String code) {
        try {
            int pluCode = Integer.parseInt(code);
            return menuItemDao.getItemByPluCode(pluCode);
        } catch (NumberFormatException e) {
            return null;
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
        log.info("Deleting order item id: {}", orderItemId);
        orderItemDao.deleteOrderItem(orderItemId);
        log.info("Order item deleted id: {}", orderItemId);
    }

    @Override
    public void processPayment(List<OrderItem> ordersToProcess, int paymentMethod, BigDecimal totalPrice,
            BigDecimal tip) {
        if (ordersToProcess == null || ordersToProcess.isEmpty()) {
            return;
        }

        log.info("Processing payment for {} items, total: {}, tip: {}", ordersToProcess.size(), totalPrice, tip);

        int waiterId = ordersToProcess.get(0).getWaiterId();

        int paymentId = paymentDao.createPayment(waiterId, paymentMethod, totalPrice, false, tip);
        log.info("Payment created with id: {}", paymentId);

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

    private BigDecimal normalizeDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        if (discount.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100");
        }

        return discount;
    }

    private BigDecimal applyDiscount(BigDecimal price, BigDecimal discountPercent) {
        BigDecimal safePrice = price != null ? price : BigDecimal.ZERO;
        BigDecimal multiplier = BigDecimal.ONE.subtract(discountPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        return safePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

}
