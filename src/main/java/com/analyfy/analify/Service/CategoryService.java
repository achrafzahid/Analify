package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.CategoryDTO;
import com.analyfy.analify.Entity.Category;
import com.analyfy.analify.Mapper.CategoryMapper;
import com.analyfy.analify.Repository.CategoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Récupérer toutes les catégories
     */
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
            .map(categoryMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer une catégorie par ID
     */
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + categoryId));
        return categoryMapper.toDto(category);
    }
    
    /**
     * Récupérer les catégories avec sections ouvertes
     */
    public List<CategoryDTO> getCategoriesWithOpenSections() {
        List<Category> categories = categoryRepository.findCategoriesWithOpenSections();
        return categories.stream()
            .map(categoryMapper::toDto)
            .collect(Collectors.toList());
    }
}