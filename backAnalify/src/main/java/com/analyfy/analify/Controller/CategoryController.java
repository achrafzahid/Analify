package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.CategoryDTO;
import com.analyfy.analify.Service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Récupérer toutes les catégories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * GET /api/categories/{id}
     * Récupérer une catégorie par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/categories/with-open-sections
     * Récupérer les catégories ayant des sections ouvertes
     */
    @GetMapping("/with-open-sections")
    public ResponseEntity<List<CategoryDTO>> getCategoriesWithOpenSections() {
        List<CategoryDTO> categories = categoryService.getCategoriesWithOpenSections();
        return ResponseEntity.ok(categories);
    }
}