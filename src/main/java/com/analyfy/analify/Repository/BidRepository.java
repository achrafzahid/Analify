package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.analyfy.analify.Entity.Bid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    // Trouver toutes les enchères d'une section
    List<Bid> findBySectionSectionIdOrderByAmountDesc(Long sectionId);
    
    // Trouver toutes les enchères d'un investisseur
    List<Bid> findByInvestorUserIdOrderByBidTimeDesc(Long investorId);
    
    // Trouver l'enchère gagnante actuelle d'une section
    @Query("SELECT b FROM Bid b WHERE b.section.sectionId = :sectionId AND b.status = 'WINNER'")
    Optional<Bid> findCurrentWinnerBid(@Param("sectionId") Long sectionId);
    
    // Trouver toutes les enchères entre deux dates
    List<Bid> findByBidTimeBetweenOrderByBidTimeDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Compter les enchères d'un investisseur
    Long countByInvestorUserId(Long investorId);
    
    // Compter les enchères sur une section
    Long countBySectionSectionId(Long sectionId);
    
    // Compter les enchéreurs uniques sur une section
    @Query("SELECT COUNT(DISTINCT b.investor.userId) FROM Bid b WHERE b.section.sectionId = :sectionId")
    Integer countUniqueBiddersBySection(@Param("sectionId") Long sectionId);
    
    // Trouver les enchères gagnantes d'un investisseur
    List<Bid> findByInvestorUserIdAndStatus(Long investorId, String status);
    
    // Vérifier si un investisseur a déjà enchéri sur une section
    boolean existsBySectionSectionIdAndInvestorUserId(Long sectionId, Long investorId);
    
    // Trouver toutes les enchères OUTBID d'une section
    List<Bid> findBySectionSectionIdAndStatus(Long sectionId, String status);
}