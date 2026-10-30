package com.phlbd.controller;

import com.phlbd.common.ApiResponse;
import com.phlbd.dto.cart.AddToCartRequest;
import com.phlbd.dto.cart.CartDto;
import com.phlbd.dto.cart.UpdateCartItemRequest;
import com.phlbd.security.SecurityUtils;
import com.phlbd.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Endpoints for managing the customer shopping cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current customer's shopping cart")
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        String userId = SecurityUtils.getCurrentUserId();
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the shopping cart")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        CartDto cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update quantity or customizations of a cart item")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @PathVariable String cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        CartDto cart = cartService.updateCartItem(userId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartDto>> removeFromCart(@PathVariable String cartItemId) {
        String userId = SecurityUtils.getCurrentUserId();
        CartDto cart = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        String userId = SecurityUtils.getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}
