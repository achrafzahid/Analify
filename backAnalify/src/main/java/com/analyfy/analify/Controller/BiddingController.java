package com.analyfy.analify.Controller;

import com.analyfy.analify.DTO.*;
import com.analyfy.analify.DTO.Bids.*;
import com.analyfy.analify.Enum.UserRole;
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
     * Étape 1: Voir toutes les catégories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
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
    public ResponseEntity<?> placeBid(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @Valid @RequestBody CreateBidRequest request) {
        try {
            // Verify user is an investor
            if (role != UserRole.INVESTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only investors can place bids");
            }
            // Ensure the bid is placed by the authenticated user
            request.setInvestorId(userId);
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
    public ResponseEntity<?> cancelBid(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @PathVariable Long bidId) {
        try {
            // Verify user is an investor
            if (role != UserRole.INVESTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only investors can cancel bids");
            }
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
    public ResponseEntity<List<BidDTO>> getAllBids(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role) {
        // Only admins can view all bids
        if (role != UserRole.ADMIN_G && role != UserRole.ADMIN_STORE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<BidDTO> bids = biddingService.getAllBids();
        return ResponseEntity.ok(bids);
    }
    
    /**
     * GET /api/bidding/my-bids
     * Récupérer toutes les enchères de l'investisseur connecté
     */
    @GetMapping("/my-bids")
    public ResponseEntity<?> getMyBids(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role) {
        try {
            // Only investors can use this endpoint
            if (role != UserRole.INVESTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This endpoint is only for investors");
            }
            List<BidDTO> bids = biddingService.getBidsByUserId(userId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET /api/bidding/my-current-winning-bids
     * Récupérer les enchères actuellement gagnantes de l'investisseur connecté (en cours)
     */
    @GetMapping("/my-current-winning-bids")
    public ResponseEntity<?> getMyCurrentWinningBids(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role) {
        try {
            // Only investors can use this endpoint
            if (role != UserRole.INVESTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This endpoint is only for investors");
            }
            List<BidDTO> bids = biddingService.getCurrentlyWinningBidsByInvestor(userId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET /api/bidding/my-winning-bids
     * Récupérer les enchères finalement gagnées de l'investisseur connecté (sections fermées)
     */
    @GetMapping("/my-winning-bids")
    public ResponseEntity<?> getMyWinningBids(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role) {
        try {
            // Only investors can use this endpoint
            if (role != UserRole.INVESTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This endpoint is only for investors");
            }
            List<BidDTO> bids = biddingService.getWinningBidsByInvestor(userId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET /api/bidding/my-possessions
     * Récupérer les sections actuellement possédées par l'investisseur connecté
     */
    @GetMapping("/my-possessions")
    public ResponseEntity<?> getMyPossessions(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role) {
        try {
            // Only investors can use this endpoint
            if (role != UserRole.INVESTOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This endpoint is only for investors");
            }
            List<SectionDTO> sections = biddingService.getPossessedSectionsByInvestor(userId);
            return ResponseEntity.ok(sections);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * GET /api/bidding/investors/{investorId}/bids
     * Récupérer toutes les enchères d'un investisseur
     */
    @GetMapping("/investors/{investorId}/bids")
    public ResponseEntity<?> getBidsByInvestor(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @PathVariable Long investorId) {
        try {
            // Investors can only view their own bids, admins can view any
            if (role == UserRole.INVESTOR && !userId.equals(investorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Investors can only view their own bids");
            }
            List<BidDTO> bids = biddingService.getBidsByUserId(investorId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/investors/{investorId}/current-winning-bids
     * Récupérer les enchères actuellement gagnantes d'un investisseur
     */
    @GetMapping("/investors/{investorId}/current-winning-bids")
    public ResponseEntity<?> getCurrentWinningBidsByInvestor(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @PathVariable Long investorId) {
        try {
            // Investors can only view their own bids, admins can view any
            if (role == UserRole.INVESTOR && !userId.equals(investorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Investors can only view their own currently winning bids");
            }
            List<BidDTO> bids = biddingService.getCurrentlyWinningBidsByInvestor(investorId);
            return ResponseEntity.ok(bids);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * GET /api/bidding/investors/{investorId}/winning-bids
     * Récupérer les enchères finalement gagnées d'un investisseur (sections fermées)
     */
    @GetMapping("/investors/{investorId}/winning-bids")
    public ResponseEntity<?> getWinningBidsByInvestor(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @PathVariable Long investorId) {
        try {
            // Investors can only view their own winning bids, admins can view any
            if (role == UserRole.INVESTOR && !userId.equals(investorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Investors can only view their own winning bids");
            }
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
     * Clôturer manuellement une section (Admin only)
     */
    @PostMapping("/sections/{sectionId}/close")
    public ResponseEntity<?> closeSection(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("role") UserRole role,
            @PathVariable Long sectionId) {
        try {
            // Only admins can manually close sections
            if (role != UserRole.ADMIN_G && role != UserRole.ADMIN_STORE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only administrators can manually close sections");
            }
            SectionDTO section = biddingService.closeSection(sectionId);
            return ResponseEntity.ok(section);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== INFORMATIONS PÉRIODE MENSUELLE ====================
    
    /**
     * GET /api/bidding/season/current
     * Obtenir les informations de la période mensuelle actuelle
     */
    @GetMapping("/season/current")
    public ResponseEntity<SeasonConfigDTO> getCurrentSeasonInfo() {
        SeasonConfigDTO periodInfo = biddingService.getCurrentSeasonInfo();
        return ResponseEntity.ok(periodInfo);
    }
}