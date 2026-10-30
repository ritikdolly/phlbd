package com.phlbd.config;

import com.phlbd.entity.*;
import com.phlbd.entity.enums.DiscountType;
import com.phlbd.entity.enums.RoleType;
import com.phlbd.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final CouponRepository couponRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking and initializing Savoria MongoDB database seed data...");

        initRoles();
        initRestaurant();
        initUsers();
        initCategoriesAndFoodItems();
        initCoupons();

        log.info("Savoria MongoDB database seed data initialization completed successfully.");
    }

    private void initRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (roleRepository.findByName(roleType).isEmpty()) {
                Role role = Role.builder()
                        .name(roleType)
                        .description("System authority: " + roleType.name())
                        .build();
                roleRepository.save(role);
            }
        }
    }

    private void initRestaurant() {
        if (restaurantRepository.findFirstByOrderByIdAsc().isEmpty()) {
            Restaurant restaurant = Restaurant.builder()
                    .name("Savoria")
                    .tagline("The Art of Indian Cuisine")
                    .description("Where ancient spices meet modern culinary artistry. Experience India reimagined.")
                    .phone("+91 98765 43210")
                    .email("reservations@savoria.in")
                    .address("42, Regal Tower, Marine Drive, Mumbai 400001")
                    .operatingHours("Mon–Sun: 12:00 PM – 11:30 PM")
                    .deliveryFee(BigDecimal.valueOf(50.00))
                    .minOrderAmount(BigDecimal.valueOf(200.00))
                    .taxPercentage(BigDecimal.valueOf(5.00))
                    .active(true)
                    .build();
            restaurantRepository.save(restaurant);
            log.info("Default Savoria restaurant settings initialized in MongoDB.");
        }
    }

    private void initUsers() {
        // 1. Owner
        if (!userRepository.existsByEmail("owner@savoria.in")) {
            User owner = User.builder()
                    .fullName("Vikramaditya Singhania")
                    .email("owner@savoria.in")
                    .passwordHash(passwordEncoder.encode("Owner@123456"))
                    .phone("+91 98200 11111")
                    .role(RoleType.ROLE_OWNER)
                    .active(true)
                    .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80")
                    .build();
            User savedOwner = userRepository.save(owner);
            cartRepository.save(Cart.builder().userId(savedOwner.getId()).build());
            log.info("Initialized default OWNER account: owner@savoria.in / Owner@123456");
        }

        // 2. Staff
        if (!userRepository.existsByEmail("staff@savoria.in")) {
            User staff = User.builder()
                    .fullName("Chef Rajesh Rawat")
                    .email("staff@savoria.in")
                    .passwordHash(passwordEncoder.encode("Staff@123456"))
                    .phone("+91 98200 22222")
                    .role(RoleType.ROLE_STAFF)
                    .active(true)
                    .avatarUrl("https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=200&q=80")
                    .build();
            User savedStaff = userRepository.save(staff);
            cartRepository.save(Cart.builder().userId(savedStaff.getId()).build());
            log.info("Initialized default STAFF account: staff@savoria.in / Staff@123456");
        }

        // 3. Delivery Man
        if (!userRepository.existsByEmail("delivery@savoria.in")) {
            User delivery = User.builder()
                    .fullName("Suresh Kumar")
                    .email("delivery@savoria.in")
                    .passwordHash(passwordEncoder.encode("Delivery@123456"))
                    .phone("+91 98200 33333")
                    .role(RoleType.ROLE_DELIVERY_MAN)
                    .active(true)
                    .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&q=80")
                    .build();
            User savedDelivery = userRepository.save(delivery);
            cartRepository.save(Cart.builder().userId(savedDelivery.getId()).build());
            log.info("Initialized default DELIVERY_MAN account: delivery@savoria.in / Delivery@123456");
        }

        // 4. Customer (matching demo user from React frontend)
        if (!userRepository.existsByEmail("arjun@example.com")) {
            Address address = Address.builder()
                    .id(UUID.randomUUID().toString())
                    .label("Home")
                    .streetLine("12, Shastri Nagar, Andheri West")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .postalCode("400053")
                    .landmark("Near Infinity Mall")
                    .contactPhone("+91 98765 43210")
                    .isDefault(true)
                    .build();

            User customer = User.builder()
                    .fullName("Arjun Mehta")
                    .email("arjun@example.com")
                    .passwordHash(passwordEncoder.encode("Customer@123456"))
                    .phone("+91 98765 43210")
                    .role(RoleType.ROLE_CUSTOMER)
                    .active(true)
                    .avatarUrl("https://i.pravatar.cc/150?img=52")
                    .addresses(List.of(address))
                    .build();

            User savedCustomer = userRepository.save(customer);
            cartRepository.save(Cart.builder().userId(savedCustomer.getId()).build());

            log.info("Initialized default CUSTOMER account: arjun@example.com / Customer@123456");
        }
    }

    private void initCategoriesAndFoodItems() {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category starters = categoryRepository.save(Category.builder()
                .name("Starters")
                .slug("starters")
                .description("Crisp, smoked, and aromatic beginnings")
                .icon("🥗")
                .displayOrder(1)
                .active(true)
                .build());

        Category mains = categoryRepository.save(Category.builder()
                .name("Mains")
                .slug("mains")
                .description("Rich gravies, slow-cooked meats, and paneer masterpieces")
                .icon("🍛")
                .displayOrder(2)
                .active(true)
                .build());

        Category breads = categoryRepository.save(Category.builder()
                .name("Breads")
                .slug("breads")
                .description("Fresh from the clay tandoor oven")
                .icon("🫓")
                .displayOrder(3)
                .active(true)
                .build());

        Category rice = categoryRepository.save(Category.builder()
                .name("Rice & Biryani")
                .slug("rice")
                .description("Fragrant basmati layered with royal spices")
                .icon("🍚")
                .displayOrder(4)
                .active(true)
                .build());

        Category desserts = categoryRepository.save(Category.builder()
                .name("Desserts")
                .slug("desserts")
                .description("Decadent sweet endings infused with cardamom and saffron")
                .icon("🍮")
                .displayOrder(5)
                .active(true)
                .build());

        Category drinks = categoryRepository.save(Category.builder()
                .name("Drinks")
                .slug("drinks")
                .description("Artisanal coolers, lassis, and signature mocktails")
                .icon("🥤")
                .displayOrder(6)
                .active(true)
                .build());

        List<FoodItem> items = List.of(
                FoodItem.builder()
                        .name("Gilafi Seekh Kebab")
                        .description("Minced lamb with saffron, herbs, and charcoal smoke — served with mint chutney and pickled onions.")
                        .price(BigDecimal.valueOf(680.00))
                        .originalPrice(BigDecimal.valueOf(820.00))
                        .categoryId(starters.getId())
                        .categoryName(starters.getName())
                        .categorySlug(starters.getSlug())
                        .imageUrl("https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=600&q=80")
                        .rating(BigDecimal.valueOf(4.9))
                        .reviewCount(342)
                        .isVeg(false)
                        .isSpicy(true)
                        .isChefSpecial(true)
                        .isBestseller(true)
                        .tags("Halal,Smoky,Non-Veg")
                        .cookTime("25 min")
                        .calories(320)
                        .available(true)
                        .customizationsJson("[{\"name\":\"Spice Level\",\"options\":[\"Mild\",\"Medium\",\"Hot\",\"Extra Hot\"],\"default\":\"Medium\"}]")
                        .build(),

                FoodItem.builder()
                        .name("Paneer Tikka Royale")
                        .description("Marinated cottage cheese with saffron-infused tandoor char, pomegranate raita, and micro herbs.")
                        .price(BigDecimal.valueOf(560.00))
                        .categoryId(starters.getId())
                        .categoryName(starters.getName())
                        .categorySlug(starters.getSlug())
                        .imageUrl("https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=600&q=80")
                        .rating(BigDecimal.valueOf(4.8))
                        .reviewCount(218)
                        .isVeg(true)
                        .isSpicy(false)
                        .isChefSpecial(false)
                        .isBestseller(true)
                        .tags("Veg,Tandoor,Gluten-Free")
                        .cookTime("20 min")
                        .calories(280)
                        .available(true)
                        .customizationsJson("[{\"name\":\"Spice Level\",\"options\":[\"Mild\",\"Medium\",\"Hot\"],\"default\":\"Medium\"}]")
                        .build(),

                FoodItem.builder()
                        .name("Murgh Makhani 1947")
                        .description("Charred tandoori chicken simmered for 12 hours in silky tomato cream, fenugreek butter, and honey.")
                        .price(BigDecimal.valueOf(720.00))
                        .originalPrice(BigDecimal.valueOf(850.00))
                        .categoryId(mains.getId())
                        .categoryName(mains.getName())
                        .categorySlug(mains.getSlug())
                        .imageUrl("https://images.unsplash.com/photo-1588166524941-3bf61a9c41db?w=600&q=80")
                        .rating(BigDecimal.valueOf(5.0))
                        .reviewCount(512)
                        .isVeg(false)
                        .isSpicy(false)
                        .isChefSpecial(true)
                        .isBestseller(true)
                        .tags("Non-Veg,Rich Gravy,Bestseller")
                        .cookTime("30 min")
                        .calories(480)
                        .available(true)
                        .customizationsJson("[{\"name\":\"Bone Option\",\"options\":[\"Boneless\",\"With Bone\"],\"default\":\"Boneless\"}]")
                        .build(),

                FoodItem.builder()
                        .name("Dum Gosht Biryani")
                        .description("Fragrant aged basmati layered with tender baby goat, brown onions, saffron milk, and rose water.")
                        .price(BigDecimal.valueOf(840.00))
                        .originalPrice(BigDecimal.valueOf(950.00))
                        .categoryId(rice.getId())
                        .categoryName(rice.getName())
                        .categorySlug(rice.getSlug())
                        .imageUrl("https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600&q=80")
                        .rating(BigDecimal.valueOf(4.9))
                        .reviewCount(480)
                        .isVeg(false)
                        .isSpicy(true)
                        .isChefSpecial(true)
                        .isBestseller(true)
                        .tags("Non-Veg,Royal Awadhi,Dum Pukht")
                        .cookTime("35 min")
                        .calories(540)
                        .available(true)
                        .build()
        );

        foodItemRepository.saveAll(items);
        log.info("Initialized {} Savoria menu items in MongoDB.", items.size());
    }

    private void initCoupons() {
        if (couponRepository.count() > 0) return;

        List<Coupon> coupons = List.of(
                Coupon.builder()
                        .code("SAVORIA50")
                        .discountType(DiscountType.PERCENTAGE)
                        .discountValue(BigDecimal.valueOf(50.00))
                        .minOrderValue(BigDecimal.valueOf(400.00))
                        .maxDiscount(BigDecimal.valueOf(250.00))
                        .validFrom(LocalDateTime.now().minusDays(1))
                        .validUntil(LocalDateTime.now().plusMonths(6))
                        .usageLimit(1000)
                        .timesUsed(12)
                        .active(true)
                        .build(),

                Coupon.builder()
                        .code("WELCOME20")
                        .discountType(DiscountType.PERCENTAGE)
                        .discountValue(BigDecimal.valueOf(20.00))
                        .minOrderValue(BigDecimal.valueOf(200.00))
                        .maxDiscount(BigDecimal.valueOf(150.00))
                        .validFrom(LocalDateTime.now().minusDays(1))
                        .validUntil(LocalDateTime.now().plusMonths(12))
                        .usageLimit(5000)
                        .timesUsed(45)
                        .active(true)
                        .build()
        );

        couponRepository.saveAll(coupons);
        log.info("Initialized {} promotional coupons in MongoDB.", coupons.size());
    }
}
