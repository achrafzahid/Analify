package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.SectionDTO;
import com.analyfy.analify.Service.SectionService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SectionController {

    private final SectionService sectionService;

    /**
     * GET /api/sections
     * Récupérer toutes les sections
     */
    @GetMapping
    public ResponseEntity<List<SectionDTO>> getAllSections() {
        List<SectionDTO> sections = sectionService.getAllSections();
        return ResponseEntity.ok(sections);
    }
    
    /**
     * GET /api/sections/{id}
     * Récupérer une section par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SectionDTO> getSectionById(@PathVariable Long id) {
        try {
            SectionDTO section = sectionService.getSectionById(id);
            return ResponseEntity.ok(section);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/sections/face/{faceId}
     * Récupérer toutes les sections d'une face
     */
    @GetMapping("/face/{faceId}")
    public ResponseEntity<List<SectionDTO>> getSectionsByFace(@PathVariable Long faceId) {
        try {
            List<SectionDTO> sections = sectionService.getSectionsByFace(faceId);
            return ResponseEntity.ok(sections);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/sections/open
     * Récupérer toutes les sections ouvertes
     */
    @GetMapping("/open")
    public ResponseEntity<List<SectionDTO>> getOpenSections() {
        List<SectionDTO> sections = sectionService.getOpenSections();
        return ResponseEntity.ok(sections);
    }
    
    /**
     * GET /api/sections/status/{status}
     * Récupérer les sections par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SectionDTO>> getSectionsByStatus(@PathVariable String status) {
        List<SectionDTO> sections = sectionService.getSectionsByStatus(status);
        return ResponseEntity.ok(sections);
    }
    
    /**
     * GET /api/sections/expiring?date=2026-03-31
     * Récupérer les sections dont la date limite approche
     */
    @GetMapping("/expiring")
    public ResponseEntity<List<SectionDTO>> getSectionsExpiringBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SectionDTO> sections = sectionService.getSectionsExpiringBefore(date);
        return ResponseEntity.ok(sections);
    }
    
    /**
     * GET /api/sections/winner/{investorId}
     * Récupérer les sections gagnées par un investisseur
     */
    @GetMapping("/winner/{investorId}")
    public ResponseEntity<List<SectionDTO>> getSectionsByWinner(@PathVariable Long investorId) {
        List<SectionDTO> sections = sectionService.getSectionsByWinner(investorId);
        return ResponseEntity.ok(sections);
    }
    
    /**
     * GET /api/sections/count/status/{status}
     * Compter les sections par statut
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countSectionsByStatus(@PathVariable String status) {
        Long count = sectionService.countSectionsByStatus(status);
        return ResponseEntity.ok(count);
    }
}