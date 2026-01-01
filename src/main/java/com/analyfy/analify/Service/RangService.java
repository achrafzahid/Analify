package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.RangDTO;
import com.analyfy.analify.Entity.Rang;
import com.analyfy.analify.Mapper.RangMapper;
import com.analyfy.analify.Repository.CategoryRepository;
import com.analyfy.analify.Repository.RangRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RangService {

    private final RangRepository rangRepository;
    private final CategoryRepository categoryRepository;
    private final RangMapper rangMapper;

    /**
     * Récupérer tous les rangs
     */
    public List<RangDTO> getAllRangs() {
        List<Rang> rangs = rangRepository.findAll();
        return rangs.stream()
            .map(rangMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer un rang par ID
     */
    public RangDTO getRangById(Long rangId) {
        Rang rang = rangRepository.findById(rangId)
            .orElseThrow(() -> new RuntimeException("Rang non trouvé avec l'ID: " + rangId));
        return rangMapper.toDto(rang);
    }
    
    /**
     * Récupérer les rangs par catégorie
     */
    public List<RangDTO> getRangsByCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + categoryId));
        
        List<Rang> rangs = rangRepository.findByCategoryCategoryId(categoryId);
        return rangs.stream()
            .map(rangMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les rangs avec sections ouvertes
     */
    public List<RangDTO> getRangsWithOpenSections() {
        List<Rang> rangs = rangRepository.findRangsWithOpenSections();
        return rangs.stream()
            .map(rangMapper::toDto)
            .collect(Collectors.toList());
    }
}