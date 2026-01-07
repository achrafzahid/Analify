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
    
    // =============== STATISTICS QUERIES ===============
    
    // Total bids value
    @Query("SELECT COALESCE(SUM(b.amount), 0.0) FROM Bid b WHERE (:investorId IS NULL OR b.investor.userId = :investorId)")
    Double calculateTotalBidsValue(@Param("investorId") Long investorId);
    
    // Count bids in date range
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.bidTime BETWEEN :startDate AND :endDate AND (:investorId IS NULL OR b.investor.userId = :investorId)")
    Long countBidsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate, 
                                @Param("investorId") Long investorId);
    
    // Average bids per section - Simplified version
    @Query("SELECT COUNT(b) FROM Bid b WHERE (:investorId IS NULL OR b.investor.userId = :investorId)")
    Long countTotalBids(@Param("investorId") Long investorId);
    
    // Most active investors (by bid count)
    @Query("SELECT CONCAT('Investor #', CAST(i.userId AS string)), i.userName, COUNT(b) FROM Bid b " +
           "JOIN b.investor i " +
           "GROUP BY i.userId, i.userName " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostActiveInvestors();
    
    // Top bidders by total amount
    @Query("SELECT CONCAT('Investor #', CAST(i.userId AS string)), i.userName, COALESCE(SUM(b.amount), 0.0) FROM Bid b " +
           "JOIN b.investor i " +
           "GROUP BY i.userId, i.userName " +
           "ORDER BY COALESCE(SUM(b.amount), 0.0) DESC")
    List<Object[]> findTopBiddersByAmount();
    
    // Bids over time (time series)
    @Query("SELECT CAST(b.bidTime AS date), COUNT(b) FROM Bid b " +
           "WHERE b.bidTime BETWEEN :startDate AND :endDate AND (:investorId IS NULL OR b.investor.userId = :investorId) " +
           "GROUP BY CAST(b.bidTime AS date) " +
           "ORDER BY CAST(b.bidTime AS date)")
    List<Object[]> findBidsOverTime(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     @Param("investorId") Long investorId);
    
    // Win rate for an investor
    @Query("SELECT " +
           "CAST(COUNT(CASE WHEN b.status = 'WINNER' THEN 1 END) AS double) / NULLIF(COUNT(b), 0) * 100 " +
           "FROM Bid b WHERE b.investor.userId = :investorId")
    Double calculateWinRate(@Param("investorId") Long investorId);
    
    // Total winning bids count
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.status = 'WINNER' AND (:investorId IS NULL OR b.investor.userId = :investorId)")
    Long countWinningBids(@Param("investorId") Long investorId);
    
    // Bids by status
    @Query("SELECT b.status, COUNT(b) FROM Bid b WHERE (:investorId IS NULL OR b.investor.userId = :investorId) GROUP BY b.status")
    List<Object[]> countBidsByStatus(@Param("investorId") Long investorId);
}