package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.review.ReviewDto;
import com.phlbd.dto.review.ReviewRequest;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews & Ratings", description = "Endpoints for customer food reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a rating & review for a completed (DELIVERED) order item")
    public ResponseEntity<ApiResponse<ReviewDto>> submitReview(@Valid @RequestBody ReviewRequest request) {
        String customerId = SecurityUtils.getCurrentUserId();
        ReviewDto review = reviewService.submitReview(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", review));
    }

    @GetMapping("/food/{foodItemId}")
    @Operation(summary = "Get reviews for a food item (Public)")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDto>>> getReviewsForFood(
            @PathVariable String foodItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<ReviewDto> reviews = reviewService.getReviewsForFoodItem(foodItemId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}
