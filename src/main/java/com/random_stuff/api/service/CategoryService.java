package com.random_stuff.api.service;
 
import com.random_stuff.api.dto.CategoryDto;
import com.random_stuff.api.entity.Category;
import com.random_stuff.api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
public class CategoryService {
 
    private final CategoryRepository categoryRepository;
 
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
            .map(CategoryDto::fromEntity)
            .toList();
    }
 
    @Transactional(readOnly = true)
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        return CategoryDto.fromEntity(category);
    }
 
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Category not found"));
    }
}