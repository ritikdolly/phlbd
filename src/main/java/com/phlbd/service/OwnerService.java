package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.auth.UserDto;
import com.phlbd.dto.delivery.AssignDeliveryRequest;
import com.phlbd.dto.delivery.DeliveryAssignmentDto;
import com.phlbd.dto.owner.OwnerDashboardStatsDto;
import com.phlbd.dto.owner.RestaurantDto;
import com.phlbd.dto.owner.RestaurantSettingsRequest;
import com.phlbd.dto.owner.StaffManagementRequest;
import com.phlbd.entity.DeliveryAssignment;
import com.phlbd.entity.Order;
import com.phlbd.entity.Restaurant;
import com.phlbd.entity.User;
import com.phlbd.entity.enums.DeliveryStatus;
import com.phlbd.entity.enums.OrderStatus;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.DeliveryAssignmentRepository;
import com.phlbd.repository.OrderRepository;
import com.phlbd.repository.RestaurantRepository;
import com.phlbd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public OwnerDashboardStatsDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();

        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalRev = calculateRevenue(allOrders);
        BigDecimal todayRev = calculateRevenue(orderRepository.findByCreatedAtBetween(todayStart, now));
        BigDecimal weekRev = calculateRevenue(orderRepository.findByCreatedAtBetween(weekStart, now));
        BigDecimal monthRev = calculateRevenue(orderRepository.findByCreatedAtBetween(monthStart, now));

        long totalOrders = allOrders.size();
        long todayOrders = orderRepository.countByCreatedAtBetween(todayStart, now);
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long preparingOrders = orderRepository.countByStatus(OrderStatus.PREPARING);
        long readyOrders = orderRepository.countByStatus(OrderStatus.READY_FOR_PICKUP);
        long outForDelivery = orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);

        long totalStaffCount = userRepository.countByRole(RoleType.ROLE_STAFF);
        long activeDeliveryMen = userRepository.countByRole(RoleType.ROLE_DELIVERY_MAN);
        long totalCustomers = userRepository.countByRole(RoleType.ROLE_CUSTOMER);

        List<Order> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc();

        return OwnerDashboardStatsDto.builder()
                .totalRevenue(totalRev)
                .todayRevenue(todayRev)
                .weekRevenue(weekRev)
                .monthRevenue(monthRev)
                .totalOrders(totalOrders)
                .todayOrders(todayOrders)
                .pendingOrders(pendingOrders)
                .preparingOrders(preparingOrders)
                .readyOrders(readyOrders)
                .outForDeliveryOrders(outForDelivery)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .totalStaffCount(totalStaffCount)
                .activeDeliveryPersonnelCount(activeDeliveryMen)
                .totalCustomersCount(totalCustomers)
                .recentOrders(recentOrders.stream().map(EntityMapper::toOrderDto).collect(Collectors.toList()))
                .build();
    }

    public Map<String, Object> getSalesReport(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;

        if ("daily".equalsIgnoreCase(period)) {
            startDate = LocalDate.now().atStartOfDay();
        } else if ("weekly".equalsIgnoreCase(period)) {
            startDate = LocalDate.now().minusDays(7).atStartOfDay();
        } else {
            startDate = LocalDate.now().minusDays(30).atStartOfDay();
            period = "monthly";
        }

        List<Order> orders = orderRepository.findByCreatedAtBetween(startDate, now);
        BigDecimal revenue = calculateRevenue(orders);
        long ordersCount = orders.size();

        Map<String, Object> report = new HashMap<>();
        report.put("period", period);
        report.put("startDate", startDate);
        report.put("endDate", now);
        report.put("totalRevenue", revenue);
        report.put("totalOrdersCount", ordersCount);
        report.put("averageOrderValue", ordersCount > 0 ? revenue.divide(BigDecimal.valueOf(ordersCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        return report;
    }

    // Staff Management
    public UserDto createStaff(StaffManagementRequest request) {
        if (request.getRole() != RoleType.ROLE_STAFF && request.getRole() != RoleType.ROLE_DELIVERY_MAN) {
            throw new BadRequestException("Role must be either ROLE_STAFF or ROLE_DELIVERY_MAN");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        User staff = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .active(request.getActive() != null ? request.getActive() : true)
                .avatarUrl("https://i.pravatar.cc/150?u=" + request.getEmail())
                .build();

        User saved = userRepository.save(staff);
        return EntityMapper.toUserDto(saved);
    }

    public UserDto updateStaff(String staffId, StaffManagementRequest request) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", "id", staffId));

        if (staff.getRole() == RoleType.ROLE_OWNER) {
            throw new BadRequestException("Owner account cannot be modified via staff management endpoint");
        }

        staff.setFullName(request.getFullName().trim());
        staff.setPhone(request.getPhone());
        if (request.getRole() != null) {
            if (request.getRole() != RoleType.ROLE_STAFF && request.getRole() != RoleType.ROLE_DELIVERY_MAN) {
                throw new BadRequestException("Role must be either ROLE_STAFF or ROLE_DELIVERY_MAN");
            }
            staff.setRole(request.getRole());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getPassword().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters");
            }
            staff.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getActive() != null) {
            staff.setActive(request.getActive());
        }

        User saved = userRepository.save(staff);
        return EntityMapper.toUserDto(saved);
    }

    public void toggleStaffStatus(String staffId, boolean active) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", "id", staffId));

        if (staff.getRole() == RoleType.ROLE_OWNER) {
            throw new BadRequestException("Owner account cannot be deactivated");
        }

        staff.setActive(active);
        userRepository.save(staff);
    }

    public PagedResponse<UserDto> getStaffMembers(RoleType role, Pageable pageable) {
        Page<User> page = (role != null)
                ? userRepository.findByRole(role, pageable)
                : userRepository.findAll(pageable);
        return PagedResponse.of(page.map(EntityMapper::toUserDto));
    }

    // Delivery Assignment
    public DeliveryAssignmentDto assignDelivery(AssignDeliveryRequest request, String assignedByUserId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        User deliveryMan = userRepository.findById(request.getDeliveryManId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery personnel", "id", request.getDeliveryManId()));

        if (deliveryMan.getRole() != RoleType.ROLE_DELIVERY_MAN) {
            throw new BadRequestException("Assigned user does not have the delivery personnel role");
        }

        if (!deliveryMan.isActive()) {
            throw new BadRequestException("Cannot assign order to deactivated delivery personnel");
        }

        User assignedBy = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assignedByUserId));

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .deliveryManId(deliveryMan.getId())
                .deliveryManName(deliveryMan.getFullName())
                .deliveryManPhone(deliveryMan.getPhone())
                .assignedById(assignedBy.getId())
                .assignedByName(assignedBy.getFullName())
                .assignedAt(LocalDateTime.now())
                .status(DeliveryStatus.ASSIGNED)
                .deliveryNotes(request.getNotes())
                .deliveryAddress(order.getDeliveryAddressSnapshot())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .build();

        DeliveryAssignment saved = deliveryAssignmentRepository.save(assignment);
        order.setDeliveryAssignment(saved);
        orderRepository.save(order);

        return EntityMapper.toDeliveryAssignmentDto(saved);
    }

    // Restaurant Settings
    public RestaurantDto getRestaurantSettings() {
        Restaurant restaurant = restaurantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant configuration not found"));
        return EntityMapper.toRestaurantDto(restaurant);
    }

    public RestaurantDto updateRestaurantSettings(RestaurantSettingsRequest request) {
        Restaurant restaurant = restaurantRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> Restaurant.builder().name("Savoria").build());

        if (request.getName() != null) restaurant.setName(request.getName());
        if (request.getTagline() != null) restaurant.setTagline(request.getTagline());
        if (request.getDescription() != null) restaurant.setDescription(request.getDescription());
        if (request.getPhone() != null) restaurant.setPhone(request.getPhone());
        if (request.getEmail() != null) restaurant.setEmail(request.getEmail());
        if (request.getAddress() != null) restaurant.setAddress(request.getAddress());
        if (request.getOperatingHours() != null) restaurant.setOperatingHours(request.getOperatingHours());
        if (request.getDeliveryFee() != null) restaurant.setDeliveryFee(request.getDeliveryFee());
        if (request.getMinOrderAmount() != null) restaurant.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getTaxPercentage() != null) restaurant.setTaxPercentage(request.getTaxPercentage());
        if (request.getActive() != null) restaurant.setActive(request.getActive());

        Restaurant saved = restaurantRepository.save(restaurant);
        return EntityMapper.toRestaurantDto(saved);
    }

    private BigDecimal calculateRevenue(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REJECTED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
