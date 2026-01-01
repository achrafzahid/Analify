package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.SectionDTO;
import com.analyfy.analify.Entity.Section;
import com.analyfy.analify.Mapper.SectionMapper;
import com.analyfy.analify.Repository.FaceRepository;
import com.analyfy.analify.Repository.SectionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final FaceRepository faceRepository;
    private final SectionMapper sectionMapper;

    /**
     * Récupérer toutes les sections
     */
    public List<SectionDTO> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer une section par ID
     */
    public SectionDTO getSectionById(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Section non trouvée avec l'ID: " + sectionId));
        return sectionMapper.toDto(section);
    }
    
    /**
     * Récupérer les sections par face
     */
    public List<SectionDTO> getSectionsByFace(Long faceId) {
        faceRepository.findById(faceId)
            .orElseThrow(() -> new RuntimeException("Face non trouvée avec l'ID: " + faceId));
        
        List<Section> sections = sectionRepository.findByFaceFaceId(faceId);
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les sections ouvertes
     */
    public List<SectionDTO> getOpenSections() {
        List<Section> sections = sectionRepository.findOpenSections();
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les sections par statut
     */
    public List<SectionDTO> getSectionsByStatus(String status) {
        List<Section> sections = sectionRepository.findByStatus(status);
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les sections dont la date limite approche
     */
    public List<SectionDTO> getSectionsExpiringBefore(LocalDate date) {
        List<Section> sections = sectionRepository.findSectionsExpiringBefore(date);
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les sections gagnées par un investisseur
     */
    public List<SectionDTO> getSectionsByWinner(Long investorId) {
        List<Section> sections = sectionRepository.findByWinnerInvestorUserId(investorId);
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Compter les sections par statut
     */
    public Long countSectionsByStatus(String status) {
        return sectionRepository.countByStatus(status);
    }
}