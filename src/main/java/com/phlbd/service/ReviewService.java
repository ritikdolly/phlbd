package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.review.ReviewDto;
import com.phlbd.dto.review.ReviewRequest;
import com.phlbd.entity.FoodItem;
import com.phlbd.entity.Order;
import com.phlbd.entity.Review;
import com.phlbd.entity.User;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.FoodItemRepository;
import com.phlbd.repository.OrderRepository;
import com.phlbd.repository.ReviewRepository;
import com.phlbd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FoodItemRepository foodItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewDto submitReview(String customerId, ReviewRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", request.getFoodItemId()));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (!order.getCustomerId().equals(customerId)) {
            throw new BadRequestException("You can only review items from orders placed by you");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Reviews can only be submitted after the order has been DELIVERED");
        }

        boolean itemInOrder = order.getItems().stream()
                .anyMatch(item -> item.getFoodItemId() != null && item.getFoodItemId().equals(foodItem.getId()));

        if (!itemInOrder) {
            throw new BadRequestException("This food item was not part of the specified order");
        }

        if (reviewRepository.existsByCustomerIdAndOrderIdAndFoodItemId(customerId, order.getId(), foodItem.getId())) {
            throw new BadRequestException("You have already reviewed this item for this order");
        }

        Review review = Review.builder()
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .customerAvatar(customer.getAvatarUrl())
                .foodItemId(foodItem.getId())
                .foodItemName(foodItem.getName())
                .orderId(order.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Recalculate average rating on FoodItem
        List<Review> allReviews = reviewRepository.findByFoodItemIdOrderByCreatedAtDesc(foodItem.getId());
        double avg = allReviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
        foodItem.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        foodItem.setReviewCount(allReviews.size());
        foodItemRepository.save(foodItem);

        return EntityMapper.toReviewDto(savedReview);
    }

    public PagedResponse<ReviewDto> getReviewsForFoodItem(String foodItemId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByFoodItemIdOrderByCreatedAtDesc(foodItemId, pageable);
        return PagedResponse.of(reviews.map(EntityMapper::toReviewDto));
    }
}
