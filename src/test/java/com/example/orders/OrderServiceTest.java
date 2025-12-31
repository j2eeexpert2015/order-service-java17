package com.example.orders;

import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import com.example.orders.model.PaymentMethod;
import com.example.orders.repository.OrderRepository;
import com.example.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }
    
    @Test
    void testCreateOrder() {
        Order order = new Order("customer@example.com", 
                                BigDecimal.valueOf(999.99), 
                                PaymentMethod.CREDIT_CARD);
        
        Order saved = orderService.createOrder(order);
        
        assertNotNull(saved.getId());
        assertEquals("customer@example.com", saved.getCustomerEmail());
        assertEquals(BigDecimal.valueOf(999.99), saved.getTotalAmount());
        assertEquals(OrderStatus.PENDING, saved.getStatus());
        assertEquals(PaymentMethod.CREDIT_CARD, saved.getPaymentMethod());
    }
    
    @Test
    void testGetOrderById() {
        Order order = new Order("test@example.com", 
                                BigDecimal.valueOf(500.00), 
                                PaymentMethod.UPI);
        Order saved = orderService.createOrder(order);
        
        Optional<Order> found = orderService.getOrderById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getCustomerEmail());
    }
    
    @Test
    void testUpdateOrderStatus() {
        Order order = new Order("update@example.com", 
                                BigDecimal.valueOf(750.00), 
                                PaymentMethod.DEBIT_CARD);
        Order saved = orderService.createOrder(order);
        
        Order updated = orderService.updateOrderStatus(saved.getId(), OrderStatus.SHIPPED);
        
        assertEquals(OrderStatus.SHIPPED, updated.getStatus());
    }
    
    @Test
    void testGetOrdersByCustomerEmail() {
        orderService.createOrder(new Order("john@example.com", 
                                           BigDecimal.valueOf(100.00), 
                                           PaymentMethod.CREDIT_CARD));
        orderService.createOrder(new Order("john@example.com", 
                                           BigDecimal.valueOf(200.00), 
                                           PaymentMethod.UPI));
        orderService.createOrder(new Order("jane@example.com", 
                                           BigDecimal.valueOf(300.00), 
                                           PaymentMethod.NET_BANKING));
        
        List<Order> johnOrders = orderService.getOrdersByCustomerEmail("john@example.com");
        
        assertEquals(2, johnOrders.size());
        assertTrue(johnOrders.stream()
                .allMatch(o -> o.getCustomerEmail().equals("john@example.com")));
    }
    
    @Test
    void testGetOrdersByStatus() {
        Order order1 = orderService.createOrder(
            new Order("user1@example.com", BigDecimal.valueOf(100.00), PaymentMethod.CREDIT_CARD)
        );
        Order order2 = orderService.createOrder(
            new Order("user2@example.com", BigDecimal.valueOf(200.00), PaymentMethod.UPI)
        );
        
        orderService.updateOrderStatus(order1.getId(), OrderStatus.SHIPPED);
        
        List<Order> pendingOrders = orderService.getOrdersByStatus(OrderStatus.PENDING);
        List<Order> shippedOrders = orderService.getOrdersByStatus(OrderStatus.SHIPPED);
        
        assertEquals(1, pendingOrders.size());
        assertEquals(1, shippedOrders.size());
        assertEquals(OrderStatus.SHIPPED, shippedOrders.getFirst().getStatus());
    }
    
    @Test
    void testGetOrderStatusMessage() {
        Order order = new Order("message@example.com", 
                                BigDecimal.valueOf(150.00), 
                                PaymentMethod.CASH_ON_DELIVERY);
        order.setStatus(OrderStatus.DELIVERED);
        
        String message = orderService.getOrderStatusMessage(order);
        
        assertEquals("Your order has been delivered successfully", message);
    }
    
    @Test
    void testDeleteOrder() {
        Order order = orderService.createOrder(
            new Order("delete@example.com", BigDecimal.valueOf(50.00), PaymentMethod.UPI)
        );
        Long orderId = order.getId();
        
        orderService.deleteOrder(orderId);
        
        Optional<Order> deleted = orderService.getOrderById(orderId);
        assertFalse(deleted.isPresent());
    }
    
    @Test
    void testDeleteNonExistentOrder() {
        assertThrows(RuntimeException.class, () -> {
            orderService.deleteOrder(99999L);
        });
    }
}
