package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.analyfy.analify.Entity.Section;

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
}