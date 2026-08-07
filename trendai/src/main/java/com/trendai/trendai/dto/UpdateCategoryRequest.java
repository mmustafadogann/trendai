package com.trendai.trendai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name cannot be empty")
    private String name;

    private String description;
}