package com.phlbd.common;

import com.phlbd.dto.auth.UserDto;
import com.phlbd.dto.cart.CartDto;
import com.phlbd.dto.cart.CartItemDto;
import com.phlbd.dto.coupon.CouponDto;
import com.phlbd.dto.customer.AddressDto;
import com.phlbd.dto.delivery.DeliveryAssignmentDto;
import com.phlbd.dto.menu.CategoryDto;
import com.phlbd.dto.menu.FoodItemDto;
import com.phlbd.dto.order.OrderDto;
import com.phlbd.dto.order.OrderItemDto;
import com.phlbd.dto.owner.RestaurantDto;
import com.phlbd.dto.payment.PaymentDto;
import com.phlbd.dto.review.ReviewDto;
import com.phlbd.entity.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class EntityMapper {

    private EntityMapper() {}

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category, int count) {
        if (category == null) return null;
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .itemCount(count)
                .build();
    }

    public static FoodItemDto toFoodItemDto(FoodItem food) {
        if (food == null) return null;
        return FoodItemDto.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .originalPrice(food.getOriginalPrice())
                .categoryId(food.getCategoryId())
                .categoryName(food.getCategoryName())
                .categorySlug(food.getCategorySlug())
                .imageUrl(food.getImageUrl())
                .rating(food.getRating())
                .reviewCount(food.getReviewCount())
                .isVeg(food.isVeg())
                .isSpicy(food.isSpicy())
                .isChefSpecial(food.isChefSpecial())
                .isBestseller(food.isBestseller())
                .cookTime(food.getCookTime())
                .calories(food.getCalories())
                .available(food.isAvailable())
                .tags(food.getTags())
                .customizationsJson(food.getCustomizationsJson())
                .build();
    }

    public static CartItemDto toCartItemDto(CartItem item) {
        if (item == null) return null;
        BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemDto.builder()
                .id(item.getId())
                .foodItemId(item.getFoodItemId())
                .foodName(item.getFoodName())
                .foodImage(item.getFoodImage())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(itemSubtotal)
                .selectedCustomizations(item.getSelectedCustomizations())
                .build();
    }

    public static CartDto toCartDto(Cart cart) {
        if (cart == null) return null;
        List<CartItemDto> itemDtos = cart.getItems() != null
                ? cart.getItems().stream().map(EntityMapper::toCartItemDto).collect(Collectors.toList())
                : Collections.emptyList();

        int totalQty = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();
        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .id(cart.getId())
                .items(itemDtos)
                .totalQuantity(totalQty)
                .subtotal(subtotal)
                .build();
    }

    public static AddressDto toAddressDto(Address address) {
        if (address == null) return null;
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .streetLine(address.getStreetLine())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .landmark(address.getLandmark())
                .contactPhone(address.getContactPhone())
                .isDefault(address.isDefault())
                .build();
    }

    public static OrderItemDto toOrderItemDto(OrderItem item) {
        if (item == null) return null;
        return OrderItemDto.builder()
                .id(item.getId())
                .foodItemId(item.getFoodItemId())
                .itemName(item.getItemName())
                .itemPrice(item.getItemPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .selectedCustomizations(item.getSelectedCustomizations())
                .build();
    }

    public static OrderDto toOrderDto(Order order) {
        if (order == null) return null;
        List<OrderItemDto> items = order.getItems() != null
                ? order.getItems().stream().map(EntityMapper::toOrderItemDto).collect(Collectors.toList())
                : Collections.emptyList();

        String deliveryManId = null;
        String deliveryManName = null;
        if (order.getDeliveryAssignment() != null) {
            deliveryManId = order.getDeliveryAssignment().getDeliveryManId();
            deliveryManName = order.getDeliveryAssignment().getDeliveryManName();
        }

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .deliveryAddressSnapshot(order.getDeliveryAddressSnapshot())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .deliveryFee(order.getDeliveryFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .couponCode(order.getCouponCode())
                .deliveryInstructions(order.getDeliveryInstructions())
                .cancellationReason(order.getCancellationReason())
                .createdAt(order.getCreatedAt())
                .items(items)
                .assignedDeliveryManId(deliveryManId)
                .assignedDeliveryManName(deliveryManName)
                .build();
    }

    public static DeliveryAssignmentDto toDeliveryAssignmentDto(DeliveryAssignment da) {
        if (da == null) return null;
        return DeliveryAssignmentDto.builder()
                .id(da.getId())
                .orderId(da.getOrderId())
                .orderNumber(da.getOrderNumber())
                .deliveryManId(da.getDeliveryManId())
                .deliveryManName(da.getDeliveryManName())
                .deliveryManPhone(da.getDeliveryManPhone())
                .assignedAt(da.getAssignedAt())
                .pickedUpAt(da.getPickedUpAt())
                .deliveredAt(da.getDeliveredAt())
                .status(da.getStatus())
                .failureReason(da.getFailureReason())
                .deliveryNotes(da.getDeliveryNotes())
                .deliveryAddress(da.getDeliveryAddress())
                .customerName(da.getCustomerName())
                .customerPhone(da.getCustomerPhone())
                .build();
    }

    public static PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) return null;
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .transactionId(payment.getTransactionId())
                .paymentMethod(payment.getPaymentMethod())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .failureReason(payment.getFailureReason())
                .build();
    }

    public static ReviewDto toReviewDto(Review review) {
        if (review == null) return null;
        return ReviewDto.builder()
                .id(review.getId())
                .foodItemId(review.getFoodItemId())
                .foodItemName(review.getFoodItemName())
                .customerId(review.getCustomerId())
                .customerName(review.getCustomerName())
                .customerAvatar(review.getCustomerAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static CouponDto toCouponDto(Coupon coupon) {
        if (coupon == null) return null;
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .maxDiscount(coupon.getMaxDiscount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .usageLimit(coupon.getUsageLimit())
                .timesUsed(coupon.getTimesUsed())
                .active(coupon.isActive())
                .build();
    }

    public static RestaurantDto toRestaurantDto(Restaurant r) {
        if (r == null) return null;
        return RestaurantDto.builder()
                .id(r.getId())
                .name(r.getName())
                .tagline(r.getTagline())
                .description(r.getDescription())
                .phone(r.getPhone())
                .email(r.getEmail())
                .address(r.getAddress())
                .operatingHours(r.getOperatingHours())
                .deliveryFee(r.getDeliveryFee())
                .minOrderAmount(r.getMinOrderAmount())
                .taxPercentage(r.getTaxPercentage())
                .active(r.isActive())
                .build();
    }
}
