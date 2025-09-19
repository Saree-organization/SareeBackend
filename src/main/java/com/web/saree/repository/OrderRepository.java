package com.web.saree.repository;

import com.web.saree.entity.Order;
import com.web.saree.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(Users user);
}