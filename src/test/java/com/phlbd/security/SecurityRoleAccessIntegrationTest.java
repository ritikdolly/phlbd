package com.phlbd.security;

import com.phlbd.config.DataInitializer;
import com.phlbd.repository.*;
import com.phlbd.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRoleAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("Public Menu Endpoint is accessible without authentication")
    void testPublicMenuEndpoint() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public Categories Endpoint is accessible without authentication")
    void testPublicCategoriesEndpoint() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Owner Dashboard Endpoint rejects unauthenticated requests with 401 Unauthorized")
    void testOwnerDashboardUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    @DisplayName("Owner Dashboard Endpoint rejects CUSTOMER role with 403 Forbidden")
    void testOwnerDashboardForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff@savoria.in", roles = {"STAFF"})
    @DisplayName("Owner Dashboard Endpoint rejects STAFF role with 403 Forbidden")
    void testOwnerDashboardForbiddenForStaff() throws Exception {
        mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner@savoria.in", roles = {"OWNER"})
    @DisplayName("Owner Dashboard Endpoint permits OWNER role with 200 OK")
    void testOwnerDashboardPermittedForOwner() throws Exception {
        mockMvc.perform(get("/api/owner/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "staff@savoria.in", roles = {"STAFF"})
    @DisplayName("Staff Dashboard Endpoint permits STAFF role with 200 OK")
    void testStaffDashboardPermittedForStaff() throws Exception {
        mockMvc.perform(get("/api/staff/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "delivery@savoria.in", roles = {"DELIVERY_MAN"})
    @DisplayName("Staff Dashboard Endpoint rejects DELIVERY_MAN role with 403 Forbidden")
    void testStaffDashboardForbiddenForDeliveryMan() throws Exception {
        mockMvc.perform(get("/api/staff/dashboard"))
                .andExpect(status().isForbidden());
    }
}
