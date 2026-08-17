package com.trendai.trendai.repository;

import com.trendai.trendai.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByActiveTrue();

    Optional<Category> findByIdAndActiveTrue(Long id);
}