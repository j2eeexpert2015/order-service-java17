package com.example.orders.repository;

import com.example.orders.model.Order;
import com.example.orders.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerEmail(String customerEmail);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerEmailAndStatus(String customerEmail, OrderStatus status);
}
