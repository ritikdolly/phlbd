package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.menu.FoodItemDto;
import com.phlbd.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Public endpoints for browsing and searching restaurant menu items")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Get all available menu items")
    public ResponseEntity<ApiResponse<List<FoodItemDto>>> getAllMenuItems() {
        List<FoodItemDto> items = menuService.getAllAvailableMenuItems();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by ID")
    public ResponseEntity<ApiResponse<FoodItemDto>> getMenuItemById(@PathVariable String id) {
        FoodItemDto item = menuService.getMenuItemById(id);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @GetMapping("/category/{categorySlug}")
    @Operation(summary = "Get menu items by category slug (or 'all')")
    public ResponseEntity<ApiResponse<List<FoodItemDto>>> getMenuItemsByCategory(@PathVariable String categorySlug) {
        List<FoodItemDto> items = menuService.getMenuItemsByCategory(categorySlug);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/search")
    @Operation(summary = "Search menu items by keyword (name, description, tags)")
    public ResponseEntity<ApiResponse<List<FoodItemDto>>> searchMenuItems(@RequestParam(required = false) String query) {
        List<FoodItemDto> items = menuService.searchMenuItems(query);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
