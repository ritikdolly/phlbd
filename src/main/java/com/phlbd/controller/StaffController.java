package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.order.OrderDto;
import com.phlbd.dto.owner.StaffDashboardDto;
import com.phlbd.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STAFF', 'OWNER')")
@Tag(name = "Staff Operations", description = "Operational endpoints for restaurant staff to manage kitchen orders")
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get staff operational dashboard (incoming, preparing, ready counts and items)")
    public ResponseEntity<ApiResponse<StaffDashboardDto>> getDashboard() {
        StaffDashboardDto dashboard = staffService.getStaffDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/orders/incoming")
    @Operation(summary = "Get all incoming PENDING orders waiting for kitchen confirmation")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getIncomingOrders() {
        List<OrderDto> orders = staffService.getIncomingOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/orders/{id}/accept")
    @Operation(summary = "Accept an incoming order (moves status to CONFIRMED)")
    public ResponseEntity<ApiResponse<OrderDto>> acceptOrder(@PathVariable String id) {
        OrderDto order = staffService.acceptOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order accepted", order));
    }

    @PostMapping("/orders/{id}/reject")
    @Operation(summary = "Reject an incoming order with reason")
    public ResponseEntity<ApiResponse<OrderDto>> rejectOrder(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Rejected by kitchen staff";
        OrderDto order = staffService.rejectOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Order rejected", order));
    }

    @PostMapping("/orders/{id}/prepare")
    @Operation(summary = "Start preparing the order (moves status to PREPARING)")
    public ResponseEntity<ApiResponse<OrderDto>> startPreparing(@PathVariable String id) {
        OrderDto order = staffService.startPreparing(id);
        return ResponseEntity.ok(ApiResponse.success("Order is now preparing in kitchen", order));
    }

    @PostMapping("/orders/{id}/ready")
    @Operation(summary = "Mark order as READY_FOR_PICKUP")
    public ResponseEntity<ApiResponse<OrderDto>> markReady(@PathVariable String id) {
        OrderDto order = staffService.markReadyForPickup(id);
        return ResponseEntity.ok(ApiResponse.success("Order is ready for pickup/delivery handover", order));
    }
}
