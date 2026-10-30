package com.phlbd.repository;

import com.phlbd.entity.Order;
import com.phlbd.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    List<Order> findTop10ByOrderByCreatedAtDesc();
}
