package com.veradotnet.folefound.stats.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {
    // 1. Nombre d'objets perdus / trouvés
    private long totalLostCount;
    private long totalFoundCount;

    // 2. Taux de restitution
    private double restitutionRate;

    // 3. Objets les plus perdus (Nom Catégorie -> Quantité)
    private Map<String, Long> topLostCategories;

    // 4. Temps moyen de restitution (en jours)
    private double averageRestitutionTimeInDays;

    // 5. Zones chaudes (Nom du Lieu -> Quantité)
    private Map<String, Long> hotZones;

    // 6. Nombre de matchings en attente de review agent
    private long totalPendingMatchings;

    // 7. Nombre total de restitutions
    private long totalRestitutionsCount;
}
