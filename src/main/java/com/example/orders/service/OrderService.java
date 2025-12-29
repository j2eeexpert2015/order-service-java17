package com.example.orders.service;

import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import com.example.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }
    
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
    
    /**
     * Get status message for an order - uses old-style switch statement (Java 17)
     * This will be refactored to use pattern matching for switch in Java 21
     */
    public String getOrderStatusMessage(Order order) {
        switch (order.getStatus()) {
            case PENDING:
                return "Your order is awaiting confirmation";
            case CONFIRMED:
                return "Your order has been confirmed and will be processed soon";
            case PROCESSING:
                return "Your order is being processed";
            case SHIPPED:
                return "Your order has been shipped and is on the way";
            case DELIVERED:
                return "Your order has been delivered successfully";
            case CANCELLED:
                return "Your order has been cancelled";
            default:
                return "Unknown order status";
        }
    }
    
    /**
     * Get recent orders - uses traditional approach
     * This will be refactored to use Sequenced Collections in Java 21
     */
    public List<Order> getRecentOrders(int limit) {
        List<Order> allOrders = orderRepository.findAll();
        // Traditional way to reverse and limit
        int size = allOrders.size();
        int startIndex = Math.max(0, size - limit);
        return allOrders.subList(startIndex, size)
            .stream()
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
    }
}
