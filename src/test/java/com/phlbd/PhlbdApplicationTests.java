package com.phlbd;

import com.phlbd.config.DataInitializer;
import com.phlbd.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhlbdApplicationTests {

    @MockBean
    private DataInitializer dataInitializer;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FoodItemRepository foodItemRepository;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private CouponRepository couponRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private RoleRepository roleRepository;

    @Test
    void contextLoads() {
    }

}
