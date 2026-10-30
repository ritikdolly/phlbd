package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.order.CancelOrderRequest;
import com.phlbd.dto.order.CheckoutRequest;
import com.phlbd.dto.order.OrderDto;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for order checkout, tracking, and cancellation")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Checkout and place an order from the user's cart")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(@Valid @RequestBody CheckoutRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        OrderDto order = orderService.checkout(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get current customer's order history")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDto>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<OrderDto> orders = orderService.getCustomerOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by order ID")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        RoleType role = SecurityUtils.getCurrentUserRole();
        OrderDto order = orderService.getOrderById(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/track/{orderNumber}")
    @Operation(summary = "Track order status by order number")
    public ResponseEntity<ApiResponse<OrderDto>> trackOrder(@PathVariable String orderNumber) {
        String userId = SecurityUtils.getCurrentUserId();
        RoleType role = SecurityUtils.getCurrentUserRole();
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber, userId, role);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order (eligible before kitchen preparation)")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable String id,
            @RequestBody(required = false) CancelOrderRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        RoleType role = SecurityUtils.getCurrentUserRole();
        if (request == null) request = new CancelOrderRequest("Cancelled by user");
        OrderDto order = orderService.cancelOrder(id, userId, role, request);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
