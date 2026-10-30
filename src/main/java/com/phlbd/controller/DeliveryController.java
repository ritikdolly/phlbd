package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.delivery.DeliveryAssignmentDto;
import com.phlbd.dto.delivery.DeliveryStatusUpdateRequest;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DELIVERY_MAN', 'OWNER')")
@Tag(name = "Delivery Operations", description = "Endpoints for delivery personnel to track and fulfill orders")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/assignments/active")
    @Operation(summary = "Get active delivery assignments for the logged-in delivery driver")
    public ResponseEntity<ApiResponse<List<DeliveryAssignmentDto>>> getActiveAssignments() {
        String deliveryManId = SecurityUtils.getCurrentUserId();
        List<DeliveryAssignmentDto> assignments = deliveryService.getActiveAssignments(deliveryManId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/assignments/history")
    @Operation(summary = "Get past delivery history for the driver")
    public ResponseEntity<ApiResponse<PagedResponse<DeliveryAssignmentDto>>> getDeliveryHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String deliveryManId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<DeliveryAssignmentDto> history = deliveryService.getDeliveryHistory(deliveryManId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PutMapping("/assignments/{id}/status")
    @Operation(summary = "Update delivery status (PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, or FAILED)")
    public ResponseEntity<ApiResponse<DeliveryAssignmentDto>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        String deliveryManId = SecurityUtils.getCurrentUserId();
        DeliveryAssignmentDto updated = deliveryService.updateDeliveryStatus(id, deliveryManId, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated successfully", updated));
    }
}
