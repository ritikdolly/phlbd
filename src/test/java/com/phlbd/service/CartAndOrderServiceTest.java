package com.phlbd.service;

import com.phlbd.dto.cart.AddToCartRequest;
import com.phlbd.dto.cart.CartDto;
import com.phlbd.entity.Cart;
import com.phlbd.entity.FoodItem;
import com.phlbd.repository.CartRepository;
import com.phlbd.repository.FoodItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartAndOrderServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("Adding available item to MongoDB cart recalculates total correctly")
    void testAddToCart() {
        String userId = "user_mongo_123";
        String foodId = "food_mongo_456";

        Cart cart = Cart.builder()
                .id("cart_123")
                .userId(userId)
                .items(new ArrayList<>())
                .build();

        FoodItem item = FoodItem.builder()
                .id(foodId)
                .name("Tandoori Paneer Tikka")
                .price(BigDecimal.valueOf(550.00))
                .available(true)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(foodItemRepository.findById(foodId)).thenReturn(Optional.of(item));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddToCartRequest request = AddToCartRequest.builder()
                .foodItemId(foodId)
                .quantity(2)
                .selectedCustomizations("Mild")
                .build();

        CartDto result = cartService.addToCart(userId, request);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getTotalQuantity());
        assertEquals(BigDecimal.valueOf(1100.00), result.getSubtotal());
    }
}

