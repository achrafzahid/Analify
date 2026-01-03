package com.analyfy.analify.Service;

import com.analyfy.analify.DTO.*;
import com.analyfy.analify.Entity.*;
import com.analyfy.analify.Mapper.*;
import com.analyfy.analify.Repository.*;
import com.analyfy.analify.DTO.Bids.*;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BiddingService {

    private final BidRepository bidRepository;
    private final SectionRepository sectionRepository;
    private final InvestorRepository investorRepository;
    private final BidMapper bidMapper;
    private final SectionMapper sectionMapper;

    // ==================== AJOUTER UNE ENCHÈRE ====================
    
    /**
     * Placer une nouvelle enchère
     */
    @Transactional
    public BidDTO addBid(CreateBidRequest request) {
        // 1. Vérifier que la section existe et est ouverte
        Section section = sectionRepository.findById(request.getSectionId())
            .orElseThrow(() -> new RuntimeException("Section non trouvée avec l'ID: " + request.getSectionId()));
        
        if (!section.getStatus().startsWith("OPEN")) {
            throw new RuntimeException("Cette section est fermée. Statut: " + section.getStatus());
        }
        
        // 2. Vérifier la date limite
        if (section.getDateDelai() != null && LocalDate.now().isAfter(section.getDateDelai())) {
            throw new RuntimeException("La date limite pour cette section est dépassée");
        }
        
        // 3. Vérifier que l'investisseur existe
        Investor investor = investorRepository.findById(request.getInvestorId())
            .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + request.getInvestorId()));
        
        // 4. Vérifier que le montant est supérieur au prix actuel
        if (request.getAmount() <= section.getCurrentPrice()) {
            throw new RuntimeException(
                String.format("Le montant (%.2f DH) doit être supérieur au prix actuel (%.2f DH)", 
                    request.getAmount(), section.getCurrentPrice())
            );
        }
        
        // 5. Mettre à jour toutes les enchères précédentes à OUTBID
        List<Bid> previousBids = bidRepository.findBySectionSectionIdAndStatus(
            section.getSectionId(), "PENDING"
        );
        for (Bid oldBid : previousBids) {
            oldBid.setStatus("OUTBID");
            bidRepository.save(oldBid);
        }
        
        // 6. Créer la nouvelle enchère avec statut WINNER
        Bid newBid = new Bid();
        newBid.setSection(section);
        newBid.setInvestor(investor);
        newBid.setAmount(request.getAmount());
        newBid.setBidTime(LocalDateTime.now());
        newBid.setStatus("PENDING");
        
        Bid savedBid = bidRepository.save(newBid);
        
        // 7. Mettre à jour le currentPrice et le statut de la section
        section.setCurrentPrice(request.getAmount());
        
        // Compter le nombre d'enchéreurs uniques
        Integer uniqueBidders = bidRepository.countUniqueBiddersBySection(section.getSectionId());
        section.setStatus("OPEN-BIDDEN BY " + uniqueBidders);
        
        sectionRepository.save(section);
        
        return bidMapper.toDto(savedBid);
    }
    
    // ==================== ANNULER UNE ENCHÈRE ====================
    
    /**
     * Annuler une enchère (seulement si elle n'est pas la gagnante actuelle)
     */
   @Transactional
public void cancelBid(Long bidId) {
    Bid bid = bidRepository.findById(bidId)
        .orElseThrow(() -> new RuntimeException("Enchère non trouvée avec l'ID: " + bidId));
    
    Section section = bid.getSection();
    
    // 1. Vérifier que la section est encore ouverte
    if (!section.getStatus().startsWith("OPEN")) {
        throw new RuntimeException("Impossible d'annuler une enchère sur une section fermée");
    }
    
    // 2. Vérifier qu'on n'annule pas une enchère déjà finalisée (WINNER)
    if ("WINNER".equals(bid.getStatus())) {
        throw new RuntimeException("Impossible d'annuler l'enchère gagnante finale");
    }
    
    // 3. Cas spécial: Si c'est l'enchère PENDING (gagnante actuelle)
    boolean wasPending = "PENDING".equals(bid.getStatus());
    
    // 4. Supprimer l'enchère
    bidRepository.delete(bid);
    
    // 5. Si c'était l'enchère PENDING, trouver et promouvoir la suivante
    if (wasPending) {
        // Trouver la prochaine meilleure enchère (la plus haute des OUTBID)
        List<Bid> remainingBids = bidRepository
            .findBySectionSectionIdAndStatus(section.getSectionId(), "OUTBID");
        
        if (!remainingBids.isEmpty()) {
            // Trier par montant décroissant et prendre la première
            Bid nextBestBid = remainingBids.stream()
                .max((b1, b2) -> Double.compare(b1.getAmount(), b2.getAmount()))
                .orElse(null);
            
            if (nextBestBid != null) {
                // Promouvoir cette enchère à PENDING
                nextBestBid.setStatus("PENDING");
                bidRepository.save(nextBestBid);
                
                // Mettre à jour le prix actuel de la section
                section.setCurrentPrice(nextBestBid.getAmount());
            }
        } else {
            // Aucune autre enchère, réinitialiser au prix de base
            section.setCurrentPrice(section.getBasePrice());
            section.setStatus("OPEN");
        }
        
        // Recalculer le nombre d'enchéreurs uniques
        Integer uniqueBidders = bidRepository
            .countUniqueBiddersBySection(section.getSectionId());
        
        if (uniqueBidders > 0) {
            section.setStatus("OPEN-BIDDEN BY " + uniqueBidders);
        } else {
            section.setStatus("OPEN");
        }
        
        sectionRepository.save(section);
    }
}
    
    // ==================== RÉCUPÉRER LES ENCHÈRES ====================
    
    /**
     * Récupérer toutes les enchères d'un investisseur
     */
    public List<BidDTO> getBidsByUserId(Long investorId) {
        investorRepository.findById(investorId)
            .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + investorId));
        
        List<Bid> bids = bidRepository.findByInvestorUserIdOrderByBidTimeDesc(investorId);
        return bids.stream()
            .map(bidMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer toutes les enchères
     */
    public List<BidDTO> getAllBids() {
        List<Bid> bids = bidRepository.findAll();
        return bids.stream()
            .map(bidMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les enchères entre deux dates
     */
    public List<BidDTO> getBidsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<Bid> bids = bidRepository.findByBidTimeBetweenOrderByBidTimeDesc(startDate, endDate);
        return bids.stream()
            .map(bidMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer l'historique des enchères pour une section
     */
    public List<BidHistoryDTO> getBidHistoryBySection(Long sectionId) {
        sectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Section non trouvée avec l'ID: " + sectionId));
        
        List<Bid> bids = bidRepository.findBySectionSectionIdOrderByAmountDesc(sectionId);
        
        return bids.stream()
            .map(bid -> new BidHistoryDTO(
                bid.getBidId(),
                bid.getAmount(),
                bid.getBidTime(),
                bid.getInvestor().getUserName(),
                bid.getStatus(),
                "WINNER".equals(bid.getStatus())
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les enchères actuellement gagnantes d'un investisseur (en cours)
     */
    public List<BidDTO> getCurrentlyWinningBidsByInvestor(Long investorId) {
        investorRepository.findById(investorId)
            .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + investorId));
        
        List<Bid> bids = bidRepository.findByInvestorUserIdAndStatus(investorId, "PENDING");
        return bids.stream()
            .map(bidMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les enchères finalement gagnées d'un investisseur (sections fermées)
     */
    public List<BidDTO> getWinningBidsByInvestor(Long investorId) {
        investorRepository.findById(investorId)
            .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + investorId));
        
        List<Bid> bids = bidRepository.findByInvestorUserIdAndStatus(investorId, "WINNER");
        return bids.stream()
            .map(bidMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer les sections actuellement possédées par un investisseur
     */
    public List<SectionDTO> getPossessedSectionsByInvestor(Long investorId) {
        Investor investor = investorRepository.findById(investorId)
            .orElseThrow(() -> new RuntimeException("Investisseur non trouvé avec l'ID: " + investorId));
        
        List<Section> sections = sectionRepository.findByWinnerInvestor(investor);
        return sections.stream()
            .map(sectionMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupérer l'enchère gagnante actuelle d'une section
     */
    public BidDTO getCurrentWinningBid(Long sectionId) {
        sectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Section non trouvée avec l'ID: " + sectionId));
        
        Bid winningBid = bidRepository.findCurrentWinnerBid(sectionId)
            .orElseThrow(() -> new RuntimeException("Aucune enchère gagnante pour cette section"));
        
        return bidMapper.toDto(winningBid);
    }

    // ==================== CLÔTURE AUTOMATIQUE ====================

    /**
     * Clôturer automatiquement les sections dont la date limite est dépassée
     * Exécuté tous les jours à minuit
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoCloseSections() {
        LocalDate today = LocalDate.now();
        List<Section> sectionsToClose = sectionRepository.findSectionsToClose(today);
        
        for (Section section : sectionsToClose) {
            closeSectionAndAssignWinner(section);
        }
    }

    /**
     * Clôturer manuellement une section
     */
    @Transactional
    public SectionDTO closeSection(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
            .orElseThrow(() -> new RuntimeException("Section non trouvée avec l'ID: " + sectionId));
        
        if (!section.getStatus().startsWith("OPEN")) {
            throw new RuntimeException("Cette section est déjà fermée");
        }
        
        closeSectionAndAssignWinner(section);
        
        return sectionMapper.toDto(section);
    }

    /**
     * Méthode privée pour clôturer une section et assigner le gagnant
     */
    private void closeSectionAndAssignWinner(Section section) {
        // Trouver l'enchère gagnante
        Bid winningBid = bidRepository.findCurrentWinnerBid(section.getSectionId())
            .orElse(null);
        
        if (winningBid != null) {
            // Assigner le gagnant
            section.setWinnerInvestor(winningBid.getInvestor());
            section.setStatus("CLOSED");
        } else {
            // Aucune enchère, remettre la section à OPEN
            section.setStatus("OPEN");
            section.setCurrentPrice(section.getBasePrice());
        }
        
        sectionRepository.save(section);
    }

    // ==================== GESTION DES PÉRIODES MENSUELLES ====================

    /**
     * Augmenter les prix de 2% pour la nouvelle période mensuelle
     * Exécuté le 1er de chaque mois à minuit
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void increasePricesForNewMonth() {
        List<Section> allSections = sectionRepository.findAll();
        
        for (Section section : allSections) {
            // Augmenter basePrice de 2%
            double newBasePrice = section.getBasePrice() * 1.02;
            section.setBasePrice(newBasePrice);
            
            // Réinitialiser currentPrice
            section.setCurrentPrice(newBasePrice);
            
            // Réinitialiser le statut
            section.setStatus("OPEN");
            
            // Définir la nouvelle date limite (dernier jour du mois actuel)
            LocalDate nextMonthStart = calculateNextMonthStart();
            section.setDateDelai(nextMonthStart.minusDays(1)); // Fin du mois actuel
            
            // Effacer le gagnant précédent
            section.setWinnerInvestor(null);
            
            sectionRepository.save(section);
        }
    }

    /**
     * Calculer la date de début du mois suivant
     */
    private LocalDate calculateNextMonthStart() {
        LocalDate today = LocalDate.now();
        return today.plusMonths(1).withDayOfMonth(1);
    }

    /**
     * Obtenir les informations de la période mensuelle actuelle
     */
    public SeasonConfigDTO getCurrentSeasonInfo() {
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        
        // Période actuelle = mois actuel
        int currentPeriod = currentMonth; // 1-12
        
        // Dates de début et fin de la période mensuelle
        LocalDate periodStartDate = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate periodEndDate = periodStartDate.plusMonths(1).minusDays(1); // Dernier jour du mois
        
        // Date d'ouverture des enchères (début du mois)
        LocalDate biddingOpenDate = periodStartDate;
        
        // Date de clôture des enchères (dernier jour du mois)
        LocalDate biddingCloseDate = periodEndDate;
        
        // Vérifier si les enchères sont ouvertes
        boolean isBiddingOpen = !today.isBefore(biddingOpenDate) && !today.isAfter(biddingCloseDate);
        
        // Jours restants jusqu'à la clôture
        int daysUntilClose = isBiddingOpen ? 
            (int) java.time.temporal.ChronoUnit.DAYS.between(today, biddingCloseDate) : 0;
        
        return new SeasonConfigDTO(
            currentMonth,
            currentPeriod,
            periodStartDate,
            periodEndDate,
            biddingOpenDate,
            biddingCloseDate,
            isBiddingOpen,
            daysUntilClose
        );
    }
}