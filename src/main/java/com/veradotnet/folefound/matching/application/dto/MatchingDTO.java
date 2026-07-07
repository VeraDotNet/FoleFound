package com.veradotnet.folefound.matching.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchingDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private MatchingStatus status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateCreated;

    // Informations simplifiées sur la déclaration de perte
    private Long lostDeclarationId;
    private String lostDeclarationItemName;
    private LocalDateTime lostDeclarationDate; // Date de l'événement (perte)
    private String lostDeclarationColor;        // Couleur de l'objet perdu
    private String lostDeclarationLocation;     // Lieu/Campus de la perte

    // Informations simplifiées sur la déclaration de trouvaille
    private Long foundDeclarationId;
    private String foundDeclarationItemName;
    private LocalDateTime foundDeclarationDate; // Date de l'événement (trouvaille)
    private String foundDeclarationColor;        // Couleur de l'objet trouvé
    private String foundDeclarationLocation;     // Lieu/Campus de la trouvaille

    // Informations communes pour la comparaison
    private Double matchingScore;                // Le score de matching (ex: 85.5)
    private String categoryName;                 // La catégorie (identique pour les deux par logique métier)
}
