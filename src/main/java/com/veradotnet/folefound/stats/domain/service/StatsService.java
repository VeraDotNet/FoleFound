package com.veradotnet.folefound.stats.domain.service;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.declaration.domain.repository.DeclarationRepo;
import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import com.veradotnet.folefound.matching.domain.repository.MatchingRepo;
import com.veradotnet.folefound.restitution.domain.repository.RestitutionRepo;
import com.veradotnet.folefound.stats.application.dto.DashboardStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final DeclarationRepo declarationRepo;

    private final RestitutionRepo restitutionRepo;

    private final MatchingRepo matchingRepo;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        // 1. Volumes globaux
        long lostCount = declarationRepo.countByType(DeclarationType.LOST);
        long foundCount = declarationRepo.countByType(DeclarationType.FOUND);

        // 2. Calcul du taux de restitution
        long resolvedLostCount = declarationRepo.countByTypeAndStatus(DeclarationType.LOST, DeclarationStatus.RESOLVED);
        double restitutionRate = lostCount > 0 ? ((double) resolvedLostCount / lostCount) * 100 : 0.0;

        // 3. Top 5 des catégories les plus perdues
        List<Object[]> rawCategories = declarationRepo.findTopLostCategories(PageRequest.of(0, 5));
        Map<String, Long> topCategories = new LinkedHashMap<>();
        rawCategories.forEach(row -> topCategories.put((String) row[0], (Long) row[1]));

        // 4. Top 5 des zones chaudes
        List<Object[]> rawZones = declarationRepo.findHotZones(PageRequest.of(0, 5));
        Map<String, Long> hotZones = new LinkedHashMap<>();
        rawZones.forEach(row -> hotZones.put((String) row[0], (Long) row[1]));

        // 5. Temps moyen de restitution (conversion secondes -> jours)
        Double avgSeconds = declarationRepo.findAverageRestitutionTimeInSeconds();
        double avgDays = avgSeconds != null ? (avgSeconds / (3600 * 24)) : 0.0;

        long pendingMatchings = matchingRepo.countByStatus(MatchingStatus.PENDING_AGENT_REVIEW);
        long totalRestitutions = restitutionRepo.count();

        // Assemblage final du DTO via le Builder
        return DashboardStatsDTO.builder()
                .totalLostCount(lostCount)
                .totalFoundCount(foundCount)
                .restitutionRate(Math.round(restitutionRate * 100.0) / 100.0) // Arrondi à 2 décimales
                .topLostCategories(topCategories)
                .averageRestitutionTimeInDays(Math.round(avgDays * 10.0) / 10.0) // Arrondi à 1 décimale
                .hotZones(hotZones)
                .totalPendingMatchings(pendingMatchings)
                .totalRestitutionsCount(totalRestitutions)
                .build();
    }
}
