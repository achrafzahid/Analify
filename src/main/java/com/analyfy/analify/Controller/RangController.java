package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.RangDTO;
import com.analyfy.analify.Service.RangService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rangs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RangController {

    private final RangService rangService;

    /**
     * GET /api/rangs
     * Récupérer tous les rangs
     */
    @GetMapping
    public ResponseEntity<List<RangDTO>> getAllRangs() {
        List<RangDTO> rangs = rangService.getAllRangs();
        return ResponseEntity.ok(rangs);
    }
    
    /**
     * GET /api/rangs/{id}
     * Récupérer un rang par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RangDTO> getRangById(@PathVariable Long id) {
        try {
            RangDTO rang = rangService.getRangById(id);
            return ResponseEntity.ok(rang);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/rangs/category/{categoryId}
     * Récupérer tous les rangs d'une catégorie
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<RangDTO>> getRangsByCategory(@PathVariable Long categoryId) {
        try {
            List<RangDTO> rangs = rangService.getRangsByCategory(categoryId);
            return ResponseEntity.ok(rangs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/rangs/with-open-sections
     * Récupérer les rangs ayant des sections ouvertes
     */
    @GetMapping("/with-open-sections")
    public ResponseEntity<List<RangDTO>> getRangsWithOpenSections() {
        List<RangDTO> rangs = rangService.getRangsWithOpenSections();
        return ResponseEntity.ok(rangs);
    }
}