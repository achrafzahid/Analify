package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.*;
import com.analyfy.analify.DTO.Bids.*;
import com.analyfy.analify.Service.BiddingService;
import com.analyfy.analify.Service.CategoryService;
import com.analyfy.analify.Service.RangService;
import com.analyfy.analify.Service.FaceService;
import com.analyfy.analify.Service.SectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bidding")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BiddingController {

    private final BiddingService biddingService;
    private final CategoryService categoryService;
    private final RangService rangService;
    private final FaceService faceService;
    private final SectionService sectionService;

    // ==================== NAVIGATION HIÉRARCHIQUE ====================
    
    /**
     * GET /api/bidding/categories
     * Étape 1: Voir toutes les catégories avec sections ouvertes
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getCategoriesWithOpenSections();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * GET /api/bidding/categories/{categoryId}
     * Récupérer une catégorie par ID
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long categoryId) {
        try {
            CategoryDTO category = categoryService.getCategoryById(categoryId);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/categories/{categoryId}/rangs
     * Étape 2: Choisir une catégorie et voir les rangs disponibles
     */
    @GetMapping("/categories/{categoryId}/rangs")
    public ResponseEntity<List<RangDTO>> getRangsByCategory(@PathVariable Long categoryId) {
        try {
            List<RangDTO> rangs = rangService.getRangsByCategory(categoryId);
            return ResponseEntity.ok(rangs);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/rangs/{rangId}/faces
     * Étape 3: Choisir un rang et voir les faces disponibles
     */
    @GetMapping("/rangs/{rangId}/faces")
    public ResponseEntity<List<FaceDTO>> getFacesByRang(@PathVariable Long rangId) {
        try {
            List<FaceDTO> faces = faceService.getFacesByRang(rangId);
            return ResponseEntity.ok(faces);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/faces/{faceId}/sections
     * Étape 4: Choisir une face et voir les sections disponibles
     */
    @GetMapping("/faces/{faceId}/sections")
    public ResponseEntity<List<SectionDTO>> getSectionsByFace(@PathVariable Long faceId) {
        try {
            List<SectionDTO> sections = sectionService.getSectionsByFace(faceId);
            return ResponseEntity.ok(sections);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/sections/{sectionId}
     * Étape 5: Voir les détails d'une section (basePrice, currentPrice, status)
     */
    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<SectionDTO> getSectionDetails(@PathVariable Long sectionId) {
        try {
            SectionDTO section = sectionService.getSectionById(sectionId);
            return ResponseEntity.ok(section);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== GESTION DES ENCHÈRES ====================
    
    /**
     * POST /api/bidding/bids
     * Placer une nouvelle enchère
     */
    @PostMapping("/bids")
    public ResponseEntity<?> placeBid(@Valid @RequestBody CreateBidRequest request) {
        try {
            BidDTO bid = biddingService.addBid(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(bid);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * DELETE /api/bidding/bids/{bidId}
     * Annuler une enchère (seulement si pas gagnante)
     */
    @DeleteMapping("/bids/{bidId}")
    public ResponseEntity<?> cancelBid(@PathVariable Long bidId) {
        try {
            biddingService.cancelBid(bidId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET /api/bidding/bids
     * Récupérer toutes les enchères
     */
    @GetMapping("/bids")
    public ResponseEntity<List<BidDTO>> getAllBids() {
        List<BidDTO> bids = biddingService.getAllBids();
        return ResponseEntity.ok(bids);
    }
    
    /**
     * GET /api/bidding/investors/{investorId}/bids
     * Récupérer toutes les enchères d'un investisseur
     */
    @GetMapping("/investors/{investorId}/bids")
    public ResponseEntity<List<BidDTO>> getBidsByInvestor(@PathVariable Long investorId) {
        try {
            List<BidDTO> bids = biddingService.getBidsByUserId(investorId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/investors/{investorId}/winning-bids
     * Récupérer les enchères gagnantes d'un investisseur
     */
    @GetMapping("/investors/{investorId}/winning-bids")
    public ResponseEntity<List<BidDTO>> getWinningBidsByInvestor(@PathVariable Long investorId) {
        try {
            List<BidDTO> bids = biddingService.getWinningBidsByInvestor(investorId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/bids/between?startDate=...&endDate=...
     * Récupérer les enchères entre deux dates
     */
    @GetMapping("/bids/between")
    public ResponseEntity<List<BidDTO>> getBidsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<BidDTO> bids = biddingService.getBidsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(bids);
    }
    
    /**
     * GET /api/bidding/sections/{sectionId}/bids
     * Récupérer l'historique des enchères pour une section
     */
    @GetMapping("/sections/{sectionId}/bids")
    public ResponseEntity<List<BidHistoryDTO>> getBidHistory(@PathVariable Long sectionId) {
        try {
            List<BidHistoryDTO> history = biddingService.getBidHistoryBySection(sectionId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/sections/{sectionId}/winner
     * Récupérer l'enchère gagnante actuelle d'une section
     */
    @GetMapping("/sections/{sectionId}/winner")
    public ResponseEntity<BidDTO> getCurrentWinner(@PathVariable Long sectionId) {
        try {
            BidDTO winningBid = biddingService.getCurrentWinningBid(sectionId);
            return ResponseEntity.ok(winningBid);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== CLÔTURE DES ENCHÈRES ====================
    
    /**
     * POST /api/bidding/sections/{sectionId}/close
     * Clôturer manuellement une section
     */
    @PostMapping("/sections/{sectionId}/close")
    public ResponseEntity<?> closeSection(@PathVariable Long sectionId) {
        try {
            SectionDTO section = biddingService.closeSection(sectionId);
            return ResponseEntity.ok(section);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== INFORMATIONS SAISON ====================
    
    /**
     * GET /api/bidding/season/current
     * Obtenir les informations de la saison actuelle
     */
    @GetMapping("/season/current")
    public ResponseEntity<SeasonConfigDTO> getCurrentSeasonInfo() {
        SeasonConfigDTO seasonInfo = biddingService.getCurrentSeasonInfo();
        return ResponseEntity.ok(seasonInfo);
    }
}