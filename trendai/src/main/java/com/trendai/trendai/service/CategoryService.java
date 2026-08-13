package com.trendai.trendai.service;

import com.trendai.trendai.dto.CategoryResponse;
import com.trendai.trendai.dto.CreateCategoryRequest;
import com.trendai.trendai.dto.UpdateCategoryRequest;
import com.trendai.trendai.entity.Category;
import com.trendai.trendai.exception.BusinessException;
import com.trendai.trendai.exception.ResourceNotFoundException;
import com.trendai.trendai.mapper.CategoryMapper;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            ProductRepository productRepository) {

        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.productRepository = productRepository;
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {

        String categoryName = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(categoryName)) {
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

        return categoryRepository.findAllByActiveTrue()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return categoryMapper.toResponse(category);
    }

    public CategoryResponse updateCategory(
            Long id,
            UpdateCategoryRequest request) {

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String categoryName = request.getName().trim();

        if (!category.getName().equalsIgnoreCase(categoryName)
                && categoryRepository.existsByNameIgnoreCase(categoryName)) {

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

        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (productRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new BusinessException(
                    "Category cannot be deleted because it contains active products"
            );
        }

        category.setActive(false);

        categoryRepository.save(category);
    }
}