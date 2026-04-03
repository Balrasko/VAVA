package dev.vavateam1.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.google.inject.Inject;

import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dto.OrderItemView;
import dev.vavateam1.model.Category;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
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
    public OrderItem createOrderFromMenu(MenuItem menuItem) {
        OrderItem orderItem = new OrderItem();

        orderItem.setMenuItemId(menuItem.getId());

        int waiterId = 0;
        if (authService != null && authService.getUser() != null) {
            waiterId = authService.getUser().getId();
        }
        orderItem.setWaiterId(waiterId);

        orderItem.setQuantity(1);
        orderItem.setDiscount(BigDecimal.ZERO);
        orderItem.setPrice(menuItem.getPrice());
        orderItem.setStatus("WAITING");

        return orderItem;
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
    public void saveTempOrders(List<OrderItemView> orderItemList) {
        for (OrderItemView view : orderItemList) {
            OrderItem item = view.getOrderItem();
            if (item.getId() != null) {
                orderItemDao.updateOrderItem(item);
            } else {
                orderItemDao.addOrderItem(item);
            }
        }
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
                orderItemDao.addOrderItem(item);
            }
        }
    }
}
