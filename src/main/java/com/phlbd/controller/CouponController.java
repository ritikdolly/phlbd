package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.coupon.ApplyCouponRequest;
import com.phlbd.dto.coupon.CouponDto;
import com.phlbd.dto.coupon.CouponRequest;
import com.phlbd.dto.coupon.CouponValidationResponse;
import com.phlbd.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons & Discounts", description = "Endpoints for promo code validation and coupon administration")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/apply")
    @Operation(summary = "Validate and calculate discount for a coupon code")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ApplyCouponRequest request) {
        CouponValidationResponse response = couponService.validateCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Get all coupons (Owner only)")
    public ResponseEntity<ApiResponse<List<CouponDto>>> getAllCoupons() {
        List<CouponDto> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Create a new coupon code (Owner only)")
    public ResponseEntity<ApiResponse<CouponDto>> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponDto coupon = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", coupon));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Update coupon details (Owner only)")
    public ResponseEntity<ApiResponse<CouponDto>> updateCoupon(
            @PathVariable String id,
            @Valid @RequestBody CouponRequest request) {
        CouponDto coupon = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully", coupon));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Delete coupon (Owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable String id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully", null));
    }
}
