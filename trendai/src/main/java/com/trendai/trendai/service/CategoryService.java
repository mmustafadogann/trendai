package com.trendai.trendai.service;

import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.dto.CategoryResponse;
import com.trendai.trendai.dto.CreateCategoryRequest;
import com.trendai.trendai.dto.UpdateCategoryRequest;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import com.trendai.trendai.mapper.CategoryMapper;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper) {

        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {

        String categoryName = request.getName().trim();

        if (categoryRepository.existsByName(categoryName)) {
            throw new BusinessException("Category already exists");
        }

        CreateCategoryRequest cleanedRequest = new CreateCategoryRequest();
        cleanedRequest.setName(categoryName);
        cleanedRequest.setDescription(request.getDescription());

        Category category = categoryMapper.toEntity(cleanedRequest);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(savedCategory);
    }

    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return categoryMapper.toResponse(category);
    }

    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String categoryName = request.getName().trim();

        if (!category.getName().equals(categoryName)
                && categoryRepository.existsByName(categoryName)) {

            throw new BusinessException("Category already exists");
        }

        UpdateCategoryRequest cleanedRequest = new UpdateCategoryRequest();
        cleanedRequest.setName(categoryName);
        cleanedRequest.setDescription(request.getDescription());

        categoryMapper.updateEntity(category, cleanedRequest);

        Category updatedCategory = categoryRepository.save(category);

        return categoryMapper.toResponse(updatedCategory);
    }

    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        categoryRepository.delete(category);
    }
}