package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.FaceDTO;
import com.analyfy.analify.Service.FaceService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FaceController {

    private final FaceService faceService;

    /**
     * GET /api/faces
     * Récupérer toutes les faces
     */
    @GetMapping
    public ResponseEntity<List<FaceDTO>> getAllFaces() {
        List<FaceDTO> faces = faceService.getAllFaces();
        return ResponseEntity.ok(faces);
    }
    
    /**
     * GET /api/faces/{id}
     * Récupérer une face par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FaceDTO> getFaceById(@PathVariable Long id) {
        try {
            FaceDTO face = faceService.getFaceById(id);
            return ResponseEntity.ok(face);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/faces/rang/{rangId}
     * Récupérer toutes les faces d'un rang
     */
    @GetMapping("/rang/{rangId}")
    public ResponseEntity<List<FaceDTO>> getFacesByRang(@PathVariable Long rangId) {
        try {
            List<FaceDTO> faces = faceService.getFacesByRang(rangId);
            return ResponseEntity.ok(faces);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/faces/with-open-sections
     * Récupérer les faces ayant des sections ouvertes
     */
    @GetMapping("/with-open-sections")
    public ResponseEntity<List<FaceDTO>> getFacesWithOpenSections() {
        List<FaceDTO> faces = faceService.getFacesWithOpenSections();
        return ResponseEntity.ok(faces);
    }
}