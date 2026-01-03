package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.analyfy.analify.Entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Trouver les catégories avec sections ouvertes
    @Query("SELECT DISTINCT c FROM Category c " +
           "JOIN c.rangs r " +
           "JOIN r.faces f " +
           "JOIN f.sections s " +
           "WHERE s.status LIKE 'OPEN%'")
    List<Category> findCategoriesWithOpenSections();
    
    // Compter les rangs par catégorie
    @Query("SELECT COUNT(r) FROM Rang r WHERE r.category.categoryId = :categoryId")
    Integer countRangsByCategory(@Param("categoryId") Long categoryId);
    
    // Compter les sections par catégorie
    @Query("SELECT COUNT(s) FROM Section s " +
           "WHERE s.face.rang.category.categoryId = :categoryId")
    Integer countSectionsByCategory(@Param("categoryId") Long categoryId);
    
    // Compter les enchères actives par catégorie
    @Query("SELECT COUNT(b) FROM Bid b " +
           "WHERE b.section.face.rang.category.categoryId = :categoryId " +
           "AND b.status = 'WINNER'")
    Integer countActiveBidsByCategory(@Param("categoryId") Long categoryId);
}