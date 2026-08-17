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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCategory() {

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Elektronik");
        request.setDescription("Elektronik Ürünler");

        Category category = new Category();
        category.setName("Elektronik");
        category.setDescription("Elektronik Ürünler");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Elektronik");
        savedCategory.setDescription("Elektronik Ürünler");

        CategoryResponse response = new CategoryResponse();
        response.setId(1L);
        response.setName("Elektronik");
        response.setDescription("Elektronik Ürünler");

        when(categoryRepository.existsByNameIgnoreCase("Elektronik"))
                .thenReturn(false);

        when(categoryMapper.toEntity(any(CreateCategoryRequest.class)))
                .thenReturn(category);

        when(categoryRepository.save(category))
                .thenReturn(savedCategory);

        when(categoryMapper.toResponse(savedCategory))
                .thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertEquals(1L, result.getId());
        assertEquals("Elektronik", result.getName());
        assertEquals("Elektronik Ürünler", result.getDescription());
    }

    @Test
    void testCreateCategoryWithDuplicateName() {

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Elektronik");
        request.setDescription("Elektronik Ürünler");

        when(categoryRepository.existsByNameIgnoreCase("Elektronik"))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> categoryService.createCategory(request)
        );
    }

    @Test
    void testUpdateCategoryNotFound() {

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Elektronik");
        request.setDescription("Elektronik Ürünler");

        when(categoryRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999L, request)
        );
    }

    @Test
    void testDeleteCategoryWithProducts() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Elektronik");

        when(categoryRepository.findById(1L))
                .thenReturn(java.util.Optional.of(category));

        when(productRepository.existsByCategoryId(1L))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> categoryService.deleteCategory(1L)
        );
    }

    @Test
    void testDeleteCategoryWithoutProducts() {

        Category category = new Category();
        category.setId(2L);
        category.setName("Kitap");

        when(categoryRepository.findById(2L))
                .thenReturn(java.util.Optional.of(category));

        when(productRepository.existsByCategoryId(2L))
                .thenReturn(false);

        categoryService.deleteCategory(2L);

        org.mockito.Mockito.verify(categoryRepository)
                .delete(category);
    }
}