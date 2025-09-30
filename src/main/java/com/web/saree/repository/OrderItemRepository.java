package com.web.saree.repository;

import com.web.saree.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query(
            value = "SELECT variant_id FROM order_items GROUP BY variant_id ORDER BY COUNT(variant_id) DESC LIMIT 4",
            nativeQuery = true)
    List<Long> findTop4VariantIds();  // we’ll limit in SQL below
}
