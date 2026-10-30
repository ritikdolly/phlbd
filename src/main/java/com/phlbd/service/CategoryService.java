package com.phlbd.service;

import com.phlbd.common.EntityMapper;
import com.phlbd.dto.menu.CategoryDto;
import com.phlbd.dto.menu.CategoryRequest;
import com.phlbd.entity.Category;
import com.phlbd.exception.BadRequestException;
import com.phlbd.exception.ResourceNotFoundException;
import com.phlbd.repository.CategoryRepository;
import com.phlbd.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;

    public List<CategoryDto> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(cat -> EntityMapper.toCategoryDto(cat, (int) foodItemRepository.countByCategoryId(cat.getId())))
                .collect(Collectors.toList());
    }

    public List<CategoryDto> getAllCategoriesForOwner() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> EntityMapper.toCategoryDto(cat, (int) foodItemRepository.countByCategoryId(cat.getId())))
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return EntityMapper.toCategoryDto(category, (int) foodItemRepository.countByCategoryId(category.getId()));
    }

    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return EntityMapper.toCategoryDto(category, (int) foodItemRepository.countByCategoryId(category.getId()));
    }

    public CategoryDto createCategory(CategoryRequest request) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9]", "-")
                : request.getName().toLowerCase().trim().replaceAll("[^a-z0-9]", "-");

        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category with slug '" + slug + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .icon(request.getIcon() != null ? request.getIcon() : "🍽️")
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Category saved = categoryRepository.save(category);
        return EntityMapper.toCategoryDto(saved, 0);
    }

    public CategoryDto updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(request.getName().trim());
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = request.getSlug().toLowerCase().trim().replaceAll("[^a-z0-9]", "-");
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new BadRequestException("Category with slug '" + newSlug + "' already exists");
            }
            category.setSlug(newSlug);
        }
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getDisplayOrder() != null) category.setDisplayOrder(request.getDisplayOrder());
        if (request.getActive() != null) category.setActive(request.getActive());

        Category updated = categoryRepository.save(category);
        return EntityMapper.toCategoryDto(updated, (int) foodItemRepository.countByCategoryId(updated.getId()));
    }

    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }
}
