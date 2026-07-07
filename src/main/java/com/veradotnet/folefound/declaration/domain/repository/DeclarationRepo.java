package com.veradotnet.folefound.declaration.domain.repository;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeclarationRepo extends JpaRepository<Declaration, Long> {

    List<Declaration> findByTypeAndStatus(DeclarationType type, DeclarationStatus status);

    List<Declaration> findByTypeAndStatusAndItemCategoryId(
            DeclarationType type,
            DeclarationStatus status,
            Long categoryId);

    Page<Declaration> findByUserId(Long userId, Pageable pageable);

    List<Declaration> findAllByStatusAndLastModifiedBefore(DeclarationStatus status, LocalDateTime limitDate);

    boolean existsByLocationId(Long id);

    // Recherche multi-critères paginée pour l'agent
    @Query("SELECT d FROM Declaration d " +
            "LEFT JOIN d.item i " +
            "WHERE (:locationId IS NULL OR d.location.id = :locationId) " +
            "AND (:campusId IS NULL OR d.location.campus.id = :campusId) " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
            "AND (:type IS NULL OR d.type = :type) " +
            "AND (:status IS NULL OR d.status = :status) " +
            "AND (cast(:startDate as date) IS NULL OR d.dateEvent >= :startDate)")
    Page<Declaration> searchDeclarationsGeneral(@Param("categoryId") Long categoryId,
                                                @Param("campusId") Long campusId,
                                                @Param("locationId") Long locationId,
                                                @Param("status") DeclarationStatus status,
                                                @Param("type") DeclarationType type,
                                                @Param("startDate") LocalDateTime startDate,
                                                Pageable pageable);

    /* Fonctions pour les stats*/
    // Compter par type (LOST / FOUND)
    long countByType(DeclarationType type);


    // Compter les pertes résolues (pour le taux de restitution)
    long countByTypeAndStatus(DeclarationType type, DeclarationStatus status);

    // Objets les plus perdus (Top Catégories)
    @Query("SELECT c.name, COUNT(d) FROM Declaration d " +
            "JOIN d.item i JOIN i.category c " +
            "WHERE d.type = 'LOST' " +
            "GROUP BY c.name " +
            "ORDER BY COUNT(d) DESC")
    List<Object[]> findTopLostCategories(Pageable pageable); // On passera un PageRequest.of(0, 5) pour le Top 5

    // Zones chaudes (Top Lieux)
    @Query("SELECT l.name, COUNT(d) FROM Declaration d " +
            "JOIN d.location l " +
            "WHERE d.type = 'LOST' " +
            "GROUP BY l.name " +
            "ORDER BY COUNT(d) DESC")
    List<Object[]> findHotZones(Pageable pageable);

    // Temps moyen de restitution en secondes (converti ensuite en jours dans le service)
    // Fonctionne sous H2/PostgreSQL en calculant l'écart entre la création et la modification finale
    @Query(value = "SELECT EXTRACT(EPOCH FROM AVG(last_modified - date_created)) FROM declaration " +
            "WHERE type = 'LOST' AND status = 'RESOLVED'", nativeQuery = true)
    Double findAverageRestitutionTimeInSeconds();
}
