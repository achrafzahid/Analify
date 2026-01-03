package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.analyfy.analify.Entity.Rang;

import java.util.List;

@Repository
public interface RangRepository extends JpaRepository<Rang, Long> {
    
    // Trouver les rangs par catégorie
    List<Rang> findByCategoryCategoryId(Long categoryId);
    
    // Compter les rangs par catégorie
    Long countByCategoryCategoryId(Long categoryId);
    
    // Trouver les rangs avec sections ouvertes
    @Query("SELECT DISTINCT r FROM Rang r JOIN r.faces f JOIN f.sections s WHERE s.status LIKE 'OPEN%'")
    List<Rang> findRangsWithOpenSections();
    
    // Compter les faces par rang
    @Query("SELECT COUNT(f) FROM Face f WHERE f.rang.rangId = :rangId")
    Integer countFacesByRang(@Param("rangId") Long rangId);
}