package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.analyfy.analify.Entity.Face;

import java.util.List;

@Repository
public interface FaceRepository extends JpaRepository<Face, Long> {
    
    // Trouver les faces par rang
    List<Face> findByRangRangId(Long rangId);
    
    // Compter les faces par rang
    Long countByRangRangId(Long rangId);
    
    // Trouver les faces avec sections ouvertes
    @Query("SELECT DISTINCT f FROM Face f JOIN f.sections s WHERE s.status LIKE 'OPEN%'")
    List<Face> findFacesWithOpenSections();
    
    // Compter les sections par face
    @Query("SELECT COUNT(s) FROM Section s WHERE s.face.faceId = :faceId")
    Integer countSectionsByFace(@Param("faceId") Long faceId);
}