package com.trendai.trendai.mapper;

import com.trendai.trendai.dto.CategoryResponse;
import com.trendai.trendai.dto.CreateCategoryRequest;
import com.trendai.trendai.dto.UpdateCategoryRequest;
import com.trendai.trendai.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(true);

        return category;
    }

    public void updateEntity(Category category, UpdateCategoryRequest request) {

        category.setName(request.getName());
        category.setDescription(request.getDescription());
    }

    public CategoryResponse toResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setActive(category.getActive());

        return response;
    }
}