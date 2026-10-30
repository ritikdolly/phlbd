package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.auth.UserDto;
import com.phlbd.dto.delivery.AssignDeliveryRequest;
import com.phlbd.dto.delivery.DeliveryAssignmentDto;
import com.phlbd.dto.menu.CategoryDto;
import com.phlbd.dto.menu.CategoryRequest;
import com.phlbd.dto.menu.FoodItemDto;
import com.phlbd.dto.menu.FoodItemRequest;
import com.phlbd.dto.order.OrderDto;
import com.phlbd.dto.order.OrderStatusUpdateRequest;
import com.phlbd.dto.owner.OwnerDashboardStatsDto;
import com.phlbd.dto.owner.RestaurantDto;
import com.phlbd.dto.owner.RestaurantSettingsRequest;
import com.phlbd.dto.owner.StaffManagementRequest;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
@Tag(name = "Owner Administration", description = "Administrative endpoints for the restaurant owner")
public class OwnerController {

    private final OwnerService ownerService;
    private final MenuService menuService;
    private final CategoryService categoryService;
    private final OrderService orderService;

    // --- Dashboard & Reports ---
    @GetMapping("/dashboard")
    @Operation(summary = "Get owner analytics dashboard metrics and revenue figures")
    public ResponseEntity<ApiResponse<OwnerDashboardStatsDto>> getDashboardStats() {
        OwnerDashboardStatsDto stats = ownerService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/reports/sales")
    @Operation(summary = "Generate sales report (period: daily, weekly, monthly)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesReport(
            @RequestParam(defaultValue = "monthly") String period) {
        Map<String, Object> report = ownerService.getSalesReport(period);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // --- Staff Management ---
    @GetMapping("/staff")
    @Operation(summary = "List staff and delivery personnel accounts")
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> getStaffMembers(
            @RequestParam(required = false) RoleType role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<UserDto> staff = ownerService.getStaffMembers(role, pageable);
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    @PostMapping("/staff")
    @Operation(summary = "Create a new staff or delivery personnel account")
    public ResponseEntity<ApiResponse<UserDto>> createStaff(@Valid @RequestBody StaffManagementRequest request) {
        UserDto staff = ownerService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff account created successfully", staff));
    }

    @PutMapping("/staff/{id}")
    @Operation(summary = "Update staff account details")
    public ResponseEntity<ApiResponse<UserDto>> updateStaff(
            @PathVariable String id,
            @Valid @RequestBody StaffManagementRequest request) {
        UserDto staff = ownerService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", staff));
    }

    @PatchMapping("/staff/{id}/status")
    @Operation(summary = "Activate or deactivate a staff account")
    public ResponseEntity<ApiResponse<Void>> toggleStaffStatus(
            @PathVariable String id,
            @RequestParam boolean active) {
        ownerService.toggleStaffStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Staff status updated successfully", null));
    }

    // --- Orders Management & Delivery Assignment ---
    @GetMapping("/orders")
    @Operation(summary = "View all restaurant orders with optional status filter")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDto>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<OrderDto> orders = orderService.getAllOrders(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        RoleType role = SecurityUtils.getCurrentUserRole();
        OrderDto order = orderService.updateOrderStatus(id, request, role);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    @PostMapping("/orders/assign-delivery")
    @Operation(summary = "Assign delivery driver to an order")
    public ResponseEntity<ApiResponse<DeliveryAssignmentDto>> assignDelivery(
            @Valid @RequestBody AssignDeliveryRequest request) {
        String ownerId = SecurityUtils.getCurrentUserId();
        DeliveryAssignmentDto assignment = ownerService.assignDelivery(request, ownerId);
        return ResponseEntity.ok(ApiResponse.success("Delivery assigned successfully", assignment));
    }

    // --- Menu Management ---
    @GetMapping("/menu")
    @Operation(summary = "Get all menu items including unavailable items")
    public ResponseEntity<ApiResponse<PagedResponse<FoodItemDto>>> getAllMenuItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<FoodItemDto> items = menuService.getAllMenuItemsForOwner(pageable);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/menu")
    @Operation(summary = "Create a new menu item")
    public ResponseEntity<ApiResponse<FoodItemDto>> createMenuItem(@Valid @RequestBody FoodItemRequest request) {
        FoodItemDto item = menuService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item created", item));
    }

    @PutMapping("/menu/{id}")
    @Operation(summary = "Update an existing menu item")
    public ResponseEntity<ApiResponse<FoodItemDto>> updateMenuItem(
            @PathVariable String id,
            @Valid @RequestBody FoodItemRequest request) {
        FoodItemDto item = menuService.updateMenuItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated", item));
    }

    @PatchMapping("/menu/{id}/availability")
    @Operation(summary = "Toggle food item availability (in stock / out of stock)")
    public ResponseEntity<ApiResponse<Void>> toggleAvailability(
            @PathVariable String id,
            @RequestParam boolean available) {
        menuService.toggleAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success("Item availability updated", null));
    }

    @DeleteMapping("/menu/{id}")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable String id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted", null));
    }

    // --- Category Management ---
    @GetMapping("/categories")
    @Operation(summary = "Get all categories for owner (including inactive)")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategoriesForOwner();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryDto category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", category));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryDto category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated", category));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    // --- Restaurant Settings ---
    @GetMapping("/settings")
    @Operation(summary = "Get restaurant settings")
    public ResponseEntity<ApiResponse<RestaurantDto>> getSettings() {
        RestaurantDto settings = ownerService.getRestaurantSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update restaurant settings (name, address, hours, delivery charges, taxes)")
    public ResponseEntity<ApiResponse<RestaurantDto>> updateSettings(
            @RequestBody RestaurantSettingsRequest request) {
        RestaurantDto settings = ownerService.updateRestaurantSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Restaurant settings updated", settings));
    }
}
