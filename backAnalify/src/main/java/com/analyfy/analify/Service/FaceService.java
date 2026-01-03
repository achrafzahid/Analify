package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.FaceDTO;
import com.analyfy.analify.Entity.Face;
import com.analyfy.analify.Mapper.FaceMapper;
import com.analyfy.analify.Repository.FaceRepository;
import com.analyfy.analify.Repository.RangRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceRepository faceRepository;
    private final RangRepository rangRepository;
    private final FaceMapper faceMapper;

    /**
     * Récupérer toutes les faces
     */
    public List<FaceDTO> getAllFaces() {
        List<Face> faces = faceRepository.findAll();
        return faces.stream()
            .map(faceMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer une face par ID
     */
    public FaceDTO getFaceById(Long faceId) {
        Face face = faceRepository.findById(faceId)
            .orElseThrow(() -> new RuntimeException("Face non trouvée avec l'ID: " + faceId));
        return faceMapper.toDto(face);
    }
    
    /**
     * Récupérer les faces par rang
     */
    public List<FaceDTO> getFacesByRang(Long rangId) {
        rangRepository.findById(rangId)
            .orElseThrow(() -> new RuntimeException("Rang non trouvé avec l'ID: " + rangId));
        
        List<Face> faces = faceRepository.findByRangRangId(rangId);
        return faces.stream()
            .map(faceMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les faces avec sections ouvertes
     */
    public List<FaceDTO> getFacesWithOpenSections() {
        List<Face> faces = faceRepository.findFacesWithOpenSections();
        return faces.stream()
            .map(faceMapper::toDto)
            .collect(Collectors.toList());
    }
}