package com.phlbd.repository;

import com.phlbd.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByFoodItemIdOrderByCreatedAtDesc(String foodItemId);
    Page<Review> findByFoodItemIdOrderByCreatedAtDesc(String foodItemId, Pageable pageable);
    boolean existsByCustomerIdAndOrderIdAndFoodItemId(String customerId, String orderId, String foodItemId);
    long countByFoodItemId(String foodItemId);
}
