package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.order.CancelOrderRequest;
import com.phlbd.dto.order.CheckoutRequest;
import com.phlbd.dto.order.OrderDto;
import com.phlbd.dto.order.OrderStatusUpdateRequest;
import com.phlbd.entity.*;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.PaymentMethod;
import com.phlbd.entity.enums.PaymentStatus;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ForbiddenException;
import com.phlbd.exception.InvalidOrderStateException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final CouponRepository couponRepository;
    private final CouponService couponService;
    private final PaymentRepository paymentRepository;

    public OrderDto checkout(String userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Shopping cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Shopping cart is empty. Please add items before checkout.");
        }

        Restaurant restaurant = restaurantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new BadRequestException("Restaurant configuration not found"));

        // Determine delivery address
        String addressId;
        String addressSnapshot;

        if (request.getAddressId() != null) {
            final String targetAddressId = request.getAddressId();
            Address address = user.getAddresses().stream()
                    .filter(a -> a.getId().equals(targetAddressId))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Selected address does not exist or belong to user"));
            addressId = address.getId();
            addressSnapshot = formatAddressSnapshot(address);
        } else if (request.getNewAddress() != null) {
            Address newAddr = Address.builder()
                    .id(UUID.randomUUID().toString())
                    .label(request.getNewAddress().getLabel())
                    .streetLine(request.getNewAddress().getStreetLine())
                    .city(request.getNewAddress().getCity())
                    .state(request.getNewAddress().getState())
                    .postalCode(request.getNewAddress().getPostalCode())
                    .landmark(request.getNewAddress().getLandmark())
                    .contactPhone(request.getNewAddress().getContactPhone() != null ? request.getNewAddress().getContactPhone() : user.getPhone())
                    .isDefault(Boolean.TRUE.equals(request.getNewAddress().getIsDefault()))
                    .build();
            user.addAddress(newAddr);
            userRepository.save(user);
            addressId = newAddr.getId();
            addressSnapshot = formatAddressSnapshot(newAddr);
        } else {
            throw new BadRequestException("Delivery address is required for checkout");
        }

        // Server-side calculation of order totals - NEVER trust frontend pricing!
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(cartItem.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", cartItem.getFoodItemId()));

            if (!foodItem.isAvailable()) {
                throw new BadRequestException("Item '" + foodItem.getName() + "' is currently unavailable.");
            }

            BigDecimal itemPrice = foodItem.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .foodItemId(foodItem.getId())
                    .itemName(foodItem.getName())
                    .itemPrice(itemPrice)
                    .quantity(cartItem.getQuantity())
                    .subtotal(itemTotal)
                    .selectedCustomizations(cartItem.getSelectedCustomizations())
                    .build();

            orderItems.add(orderItem);
        }

        if (subtotal.compareTo(restaurant.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Minimum order amount is " + restaurant.getMinOrderAmount() + " INR");
        }

        // Calculate tax based on restaurant tax percentage
        BigDecimal taxAmount = subtotal.multiply(restaurant.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal deliveryFee = restaurant.getDeliveryFee();

        // Calculate discount from coupon if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            String code = request.getCouponCode().toUpperCase().trim();
            Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new BadRequestException("Invalid coupon code: " + code));

            if (!coupon.isValidForAmount(subtotal)) {
                throw new BadRequestException("Coupon '" + code + "' is not valid for this order amount");
            }

            discountAmount = coupon.calculateDiscount(subtotal);
            appliedCouponCode = coupon.getCode();
            couponService.recordCouponUsage(appliedCouponCode);
        }

        BigDecimal totalAmount = subtotal.add(taxAmount).add(deliveryFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // Generate human-friendly order number: SAV-YYYYMMDD-XXXX
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String orderNumber = "SAV-" + datePart + "-" + randomPart;

        PaymentStatus initialPaymentStatus = PaymentStatus.PENDING;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(user.getId())
                .customerName(user.getFullName())
                .customerEmail(user.getEmail())
                .customerPhone(user.getPhone())
                .restaurantId(restaurant.getId())
                .addressId(addressId)
                .deliveryAddressSnapshot(addressSnapshot)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .deliveryFee(deliveryFee)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(initialPaymentStatus)
                .couponCode(appliedCouponCode)
                .deliveryInstructions(request.getDeliveryInstructions())
                .items(orderItems)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create initial Payment document
        Payment payment = Payment.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .paymentMethod(request.getPaymentMethod())
                .amount(totalAmount)
                .currency("INR")
                .status(initialPaymentStatus)
                .build();
        payment = paymentRepository.save(payment);
        savedOrder.setPayment(payment);
        orderRepository.save(savedOrder);

        // Clear user's shopping cart after order placement
        cart.clearItems();
        cartRepository.save(cart);

        return EntityMapper.toOrderDto(savedOrder);
    }

    public OrderDto getOrderById(String orderId, String currentUserId, RoleType role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        validateOrderAccess(order, currentUserId, role);
        return EntityMapper.toOrderDto(order);
    }

    public OrderDto getOrderByOrderNumber(String orderNumber, String currentUserId, RoleType role) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        validateOrderAccess(order, currentUserId, role);
        return EntityMapper.toOrderDto(order);
    }

    public PagedResponse<OrderDto> getCustomerOrders(String customerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return PagedResponse.of(orders.map(EntityMapper::toOrderDto));
    }

    public OrderDto cancelOrder(String orderId, String currentUserId, RoleType role, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        boolean isOwner = role == RoleType.ROLE_OWNER;
        boolean isCustomer = role == RoleType.ROLE_CUSTOMER && order.getCustomerId().equals(currentUserId);

        if (!isOwner && !isCustomer) {
            throw new ForbiddenException("You are not authorized to cancel this order");
        }

        // Customer can only cancel when order is in PENDING state
        if (isCustomer && order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order cannot be cancelled once preparation has started. Current status: " + order.getStatus());
        }

        if (order.getStatus().isTerminal()) {
            throw new InvalidOrderStateException("Order is already finalized: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(request.getReason() != null ? request.getReason() : "Cancelled by user");

        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.COMPLETED) {
            order.getPayment().setStatus(PaymentStatus.REFUNDED);
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(order.getPayment());
        }

        Order saved = orderRepository.save(order);
        return EntityMapper.toOrderDto(saved);
    }

    public OrderDto updateOrderStatus(String orderId, OrderStatusUpdateRequest request, RoleType updaterRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                    String.format("Invalid order status transition from %s to %s", currentStatus, newStatus)
            );
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REJECTED) {
            order.setCancellationReason(request.getReason());
        }

        Order saved = orderRepository.save(order);
        return EntityMapper.toOrderDto(saved);
    }

    public PagedResponse<OrderDto> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return PagedResponse.of(page.map(EntityMapper::toOrderDto));
    }

    private void validateOrderAccess(Order order, String currentUserId, RoleType role) {
        if (role == RoleType.ROLE_OWNER || role == RoleType.ROLE_STAFF) {
            return;
        }
        if (role == RoleType.ROLE_DELIVERY_MAN) {
            if (order.getDeliveryAssignment() == null ||
                !order.getDeliveryAssignment().getDeliveryManId().equals(currentUserId)) {
                throw new ForbiddenException("Delivery personnel can only view assigned orders");
            }
            return;
        }
        if (role == RoleType.ROLE_CUSTOMER && !order.getCustomerId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: You cannot view orders belonging to other customers");
        }
    }

    private String formatAddressSnapshot(Address a) {
        StringBuilder sb = new StringBuilder();
        sb.append(a.getLabel()).append(": ").append(a.getStreetLine());
        if (a.getLandmark() != null && !a.getLandmark().isBlank()) {
            sb.append(", Near ").append(a.getLandmark());
        }
        sb.append(", ").append(a.getCity()).append(", ").append(a.getPostalCode());
        if (a.getContactPhone() != null && !a.getContactPhone().isBlank()) {
            sb.append(" | Phone: ").append(a.getContactPhone());
        }
        return sb.toString();
    }
}
