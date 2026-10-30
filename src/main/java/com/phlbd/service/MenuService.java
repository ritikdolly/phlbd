package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.common.PagedResponse;
import com.phlbd.dto.menu.FoodItemDto;
import com.phlbd.dto.menu.FoodItemRequest;
import com.phlbd.entity.Category;
import com.phlbd.entity.FoodItem;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.CategoryRepository;
import com.phlbd.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final FoodItemRepository foodItemRepository;
    private final CategoryRepository categoryRepository;

    public List<FoodItemDto> getAllAvailableMenuItems() {
        return foodItemRepository.findByAvailableTrue().stream()
                .map(EntityMapper::toFoodItemDto)
                .collect(Collectors.toList());
    }

    public FoodItemDto getMenuItemById(String id) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", id));
        return EntityMapper.toFoodItemDto(item);
    }

    public List<FoodItemDto> getMenuItemsByCategory(String categorySlug) {
        if ("all".equalsIgnoreCase(categorySlug)) {
            return getAllAvailableMenuItems();
        }
        return foodItemRepository.findByCategorySlugAndAvailableTrue(categorySlug).stream()
                .map(EntityMapper::toFoodItemDto)
                .collect(Collectors.toList());
    }

    public List<FoodItemDto> searchMenuItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllAvailableMenuItems();
        }
        return foodItemRepository.searchMenu(query.trim()).stream()
                .map(EntityMapper::toFoodItemDto)
                .collect(Collectors.toList());
    }

    public PagedResponse<FoodItemDto> getAllMenuItemsForOwner(Pageable pageable) {
        Page<FoodItem> page = foodItemRepository.findAll(pageable);
        return PagedResponse.of(page.map(EntityMapper::toFoodItemDto));
    }

    public FoodItemDto createMenuItem(FoodItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        FoodItem item = FoodItem.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categorySlug(category.getSlug())
                .imageUrl(request.getImageUrl())
                .rating(BigDecimal.valueOf(5.0))
                .reviewCount(0)
                .isVeg(request.getIsVeg() != null ? request.getIsVeg() : true)
                .isSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false)
                .isChefSpecial(request.getIsChefSpecial() != null ? request.getIsChefSpecial() : false)
                .isBestseller(request.getIsBestseller() != null ? request.getIsBestseller() : false)
                .cookTime(request.getCookTime())
                .calories(request.getCalories())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .tags(request.getTags())
                .customizationsJson(request.getCustomizationsJson())
                .build();

        FoodItem saved = foodItemRepository.save(item);
        return EntityMapper.toFoodItemDto(saved);
    }

    public FoodItemDto updateMenuItem(String id, FoodItemRequest request) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", id));

        if (request.getCategoryId() != null && !request.getCategoryId().equals(item.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            item.setCategoryId(category.getId());
            item.setCategoryName(category.getName());
            item.setCategorySlug(category.getSlug());
        }

        item.setName(request.getName().trim());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setOriginalPrice(request.getOriginalPrice());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsVeg() != null) item.setVeg(request.getIsVeg());
        if (request.getIsSpicy() != null) item.setSpicy(request.getIsSpicy());
        if (request.getIsChefSpecial() != null) item.setChefSpecial(request.getIsChefSpecial());
        if (request.getIsBestseller() != null) item.setBestseller(request.getIsBestseller());
        if (request.getCookTime() != null) item.setCookTime(request.getCookTime());
        if (request.getCalories() != null) item.setCalories(request.getCalories());
        if (request.getAvailable() != null) item.setAvailable(request.getAvailable());
        if (request.getTags() != null) item.setTags(request.getTags());
        if (request.getCustomizationsJson() != null) item.setCustomizationsJson(request.getCustomizationsJson());

        FoodItem updated = foodItemRepository.save(item);
        return EntityMapper.toFoodItemDto(updated);
    }

    public void toggleAvailability(String id, boolean available) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", id));
        item.setAvailable(available);
        foodItemRepository.save(item);
    }

    public void deleteMenuItem(String id) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodItem", "id", id));
        foodItemRepository.delete(item);
    }
}
