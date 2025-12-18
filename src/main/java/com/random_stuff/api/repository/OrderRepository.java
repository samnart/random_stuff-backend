package com.random_stuff.api.repository;
 
import com.random_stuff.api.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
 
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
 
    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
 
    List<Order> findByStatus(Order.OrderStatus status);
 
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}