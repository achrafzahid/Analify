package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.analyfy.analify.Entity.Section;
import com.analyfy.analify.Entity.Investor;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    
    // Trouver les sections par face
    List<Section> findByFaceFaceId(Long faceId);
    
    // Trouver les sections par statut
    List<Section> findByStatus(String status);
    
    // Trouver les sections ouvertes
    @Query("SELECT s FROM Section s WHERE s.status LIKE 'OPEN%'")
    List<Section> findOpenSections();
    
    // Trouver les sections dont la date limite approche
    @Query("SELECT s FROM Section s WHERE s.dateDelai <= :date AND s.status LIKE 'OPEN%'")
    List<Section> findSectionsExpiringBefore(@Param("date") LocalDate date);
    
    // Trouver les sections par investisseur gagnant
    List<Section> findByWinnerInvestorUserId(Long investorId);
    
    // Trouver les sections par investisseur gagnant (entity)
    List<Section> findByWinnerInvestor(Investor investor);
    
    // Compter les sections par statut
    Long countByStatus(String status);
    
    // Compter les sections ouvertes par face
    @Query("SELECT COUNT(s) FROM Section s WHERE s.face.faceId = :faceId AND s.status LIKE 'OPEN%'")
    Integer countOpenSectionsByFace(@Param("faceId") Long faceId);
    
    // Compter les sections fermées par face
    Long countByFaceFaceIdAndStatus(Long faceId, String status);
    
    // Trouver les sections à clôturer automatiquement (date dépassée)
    @Query("SELECT s FROM Section s WHERE s.dateDelai < :today AND s.status LIKE 'OPEN%'")
    List<Section> findSectionsToClose(@Param("today") LocalDate today);
    
    // =============== STATISTICS QUERIES ===============
    
    // Total value of all sections
    @Query("SELECT COALESCE(SUM(s.currentPrice), 0.0) FROM Section s WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId)")
    Double calculateTotalSectionValue(@Param("investorId") Long investorId);
    
    // Total value by status
    @Query("SELECT COALESCE(SUM(s.currentPrice), 0.0) FROM Section s WHERE s.status = :status AND (:investorId IS NULL OR s.winnerInvestor.userId = :investorId)")
    Double calculateTotalValueByStatus(@Param("status") String status, @Param("investorId") Long investorId);
    
    // Average section price
    @Query("SELECT COALESCE(AVG(s.currentPrice), 0.0) FROM Section s WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId)")
    Double calculateAverageSectionPrice(@Param("investorId") Long investorId);
    
    // Count sections by status
    @Query("SELECT s.status, COUNT(s) FROM Section s WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) GROUP BY s.status")
    List<Object[]> countSectionsByStatus(@Param("investorId") Long investorId);
    
    // Sections by category (through Rang -> Face -> Section)
    @Query("SELECT c.categoryName, COUNT(s) FROM Section s " +
           "JOIN s.face f JOIN f.rang r JOIN r.category c " +
           "WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "GROUP BY c.categoryName")
    List<Object[]> countSectionsByCategory(@Param("investorId") Long investorId);
    
    // Total value by category
    @Query("SELECT c.categoryName, COALESCE(SUM(s.currentPrice), 0.0) FROM Section s " +
           "JOIN s.face f JOIN f.rang r JOIN r.category c " +
           "WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "GROUP BY c.categoryName")
    List<Object[]> calculateValueByCategory(@Param("investorId") Long investorId);
    
    // Total value by face
    @Query("SELECT f.faceName, COALESCE(SUM(s.currentPrice), 0.0) FROM Section s " +
           "JOIN s.face f " +
           "WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "GROUP BY f.faceName")
    List<Object[]> calculateValueByFace(@Param("investorId") Long investorId);
    
    // Total value by rang
    @Query("SELECT r.rangName, COALESCE(SUM(s.currentPrice), 0.0) FROM Section s " +
           "JOIN s.face f JOIN f.rang r " +
           "WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "GROUP BY r.rangName")
    List<Object[]> calculateValueByRang(@Param("investorId") Long investorId);
    
    // Most competitive sections (by bid count) - requires join with Bid
    @Query("SELECT s.sectionName, COUNT(b) FROM Section s " +
           "LEFT JOIN s.bids b " +
           "WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "GROUP BY s.sectionId, s.sectionName " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> findMostCompetitiveSections(@Param("investorId") Long investorId);
    
    // Highest value sections
    @Query("SELECT s.sectionName, s.currentPrice FROM Section s " +
           "WHERE (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "ORDER BY s.currentPrice DESC")
    List<Object[]> findHighestValueSections(@Param("investorId") Long investorId);
    
    // Sections opened by month (for time series)
    @Query("SELECT FUNCTION('TO_CHAR', s.dateDelai, 'YYYY-MM'), COUNT(s) FROM Section s " +
           "WHERE s.dateDelai IS NOT NULL AND (:investorId IS NULL OR s.winnerInvestor.userId = :investorId) " +
           "GROUP BY FUNCTION('TO_CHAR', s.dateDelai, 'YYYY-MM') " +
           "ORDER BY FUNCTION('TO_CHAR', s.dateDelai, 'YYYY-MM')")
    List<Object[]> countSectionsOpenedByMonth(@Param("investorId") Long investorId);
    
    // Average price increase (current - base)
    @Query("SELECT COALESCE(AVG(s.currentPrice - s.basePrice), 0.0) FROM Section s " +
           "WHERE s.currentPrice > s.basePrice AND (:investorId IS NULL OR s.winnerInvestor.userId = :investorId)")
    Double calculateAveragePriceIncrease(@Param("investorId") Long investorId);
    
    // Expected revenue (active sections)
    @Query("SELECT COALESCE(SUM(s.currentPrice), 0.0) FROM Section s " +
           "WHERE s.status LIKE 'OPEN%' AND (:investorId IS NULL OR s.winnerInvestor.userId = :investorId)")
    Double calculateExpectedRevenue(@Param("investorId") Long investorId);
    
    // Actual revenue (won sections)
    @Query("SELECT COALESCE(SUM(s.currentPrice), 0.0) FROM Section s " +
           "WHERE s.winnerInvestor IS NOT NULL AND (:investorId IS NULL OR s.winnerInvestor.userId = :investorId)")
    Double calculateActualRevenue(@Param("investorId") Long investorId);
}