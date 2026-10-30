package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.dto.cart.AddToCartRequest;
import com.phlbd.dto.cart.CartDto;
import com.phlbd.dto.cart.UpdateCartItemRequest;
import com.phlbd.entity.Cart;
import com.phlbd.entity.CartItem;
import com.phlbd.entity.FoodItem;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.CartRepository;
import com.phlbd.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final FoodItemRepository foodItemRepository;

    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().userId(userId).build();
            return cartRepository.save(newCart);
        });
    }

    public CartDto getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return EntityMapper.toCartDto(cart);
    }

    public CartDto addToCart(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", request.getFoodItemId()));

        if (!foodItem.isAvailable()) {
            throw new BadRequestException("This food item is currently out of stock");
        }

        // Check if matching item exists with same customizations
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getFoodItemId().equals(foodItem.getId()) &&
                               Objects.equals(item.getSelectedCustomizations(), request.getSelectedCustomizations()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .id(UUID.randomUUID().toString())
                    .foodItemId(foodItem.getId())
                    .foodName(foodItem.getName())
                    .foodImage(foodItem.getImageUrl())
                    .quantity(request.getQuantity())
                    .unitPrice(foodItem.getPrice())
                    .selectedCustomizations(request.getSelectedCustomizations())
                    .build();
            cart.addItem(newItem);
        }

        Cart updatedCart = cartRepository.save(cart);
        return EntityMapper.toCartDto(updatedCart);
    }

    public CartDto updateCartItem(String userId, String cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (request.getQuantity() <= 0) {
            cart.removeItem(cartItemId);
        } else {
            item.setQuantity(request.getQuantity());
            if (request.getSelectedCustomizations() != null) {
                item.setSelectedCustomizations(request.getSelectedCustomizations());
            }
        }

        Cart updatedCart = cartRepository.save(cart);
        return EntityMapper.toCartDto(updatedCart);
    }

    public CartDto removeFromCart(String userId, String cartItemId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(cartItemId);
        Cart updatedCart = cartRepository.save(cart);
        return EntityMapper.toCartDto(updatedCart);
    }

    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clearItems();
        cartRepository.save(cart);
    }
}
