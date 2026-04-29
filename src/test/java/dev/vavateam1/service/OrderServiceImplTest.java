package dev.vavateam1.service;

import dev.vavateam1.dao.CategoryDao;
import dev.vavateam1.dao.MenuItemDao;
import dev.vavateam1.dao.OrderItemDao;
import dev.vavateam1.dao.PaymentDao;
import dev.vavateam1.dto.CreateOrder;
import dev.vavateam1.model.MenuItem;
import dev.vavateam1.model.OrderItem;
import dev.vavateam1.model.Table;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock MenuItemDao menuItemDao;
    @Mock OrderItemDao orderItemDao;
    @Mock CategoryDao categoryDao;
    @Mock PaymentDao paymentDao;
    @Mock AuthService authService;

    @InjectMocks
    OrderServiceImpl orderService;

    @Test
    void getItemByPluCode_validNumber_callsDao() {
        MenuItem fakeItem = new MenuItem();
        when(menuItemDao.getItemByPluCode(42)).thenReturn(fakeItem);

        MenuItem result = orderService.getItemByPluCode("42");

        assertEquals(fakeItem, result);
    }

    @Test
    void getItemByPluCode_invalidString_returnsNull() {
        MenuItem result = orderService.getItemByPluCode("abc");
        assertNull(result);
    }

    @Test
    void getItemByPluCode_emptyString_returnsNull() {
        MenuItem result = orderService.getItemByPluCode("");
        assertNull(result);
    }

    @Test
    void createOrderFromMenu_nullTable_throwsIllegalArgument() {
        MenuItem menuItem = new MenuItem();
        menuItem.setName("Pizza");
        menuItem.setPrice(new BigDecimal("9.99"));

        assertThrows(IllegalArgumentException.class,
            () -> orderService.createOrderFromMenu(menuItem, null));
    }

    @Test
    void createOrderFromMenu_menuDiscount_appliesDiscountToCreatedOrder() {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(10);
        menuItem.setName("Pizza");
        menuItem.setPrice(new BigDecimal("10.00"));
        menuItem.setDiscount(new BigDecimal("25"));

        Table table = new Table();
        table.setId(4);

        OrderItem createdOrderItem = new OrderItem();
        createdOrderItem.setId(99);
        when(orderItemDao.createOrderItem(any(CreateOrder.class))).thenReturn(createdOrderItem);

        orderService.createOrderFromMenu(menuItem, table);

        ArgumentCaptor<CreateOrder> captor = ArgumentCaptor.forClass(CreateOrder.class);
        verify(orderItemDao).createOrderItem(captor.capture());
        CreateOrder createOrder = captor.getValue();

        assertEquals(new BigDecimal("25"), createOrder.getDiscount());
        assertEquals(new BigDecimal("7.50"), createOrder.getPrice());
    }
}
