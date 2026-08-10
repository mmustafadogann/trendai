package com.trendai.trendai.service;

import com.trendai.trendai.dto.CategoryResponse;
import com.trendai.trendai.dto.CreateCategoryRequest;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.dto.UpdateCategoryRequest;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {

        // DTO -> Entity (Manual Mapping)
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);

        // Entity -> DTO (Manual Mapping)
        CategoryResponse response = new CategoryResponse();
        response.setId(savedCategory.getId());
        response.setName(savedCategory.getName());
        response.setDescription(savedCategory.getDescription());

        return response;
    }

    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll().stream().map(category -> {
            CategoryResponse response = new CategoryResponse();

            response.setId(category.getId());
            response.setName(category.getName());
            response.setDescription(category.getDescription());

            return response;
        }).toList();
    }

    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        return response;
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        CategoryResponse response = new CategoryResponse();

        response.setId(updatedCategory.getId());
        response.setName(updatedCategory.getName());
        response.setDescription(updatedCategory.getDescription());

        return response;
    }
}