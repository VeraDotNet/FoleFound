package com.veradotnet.folefound.declaration.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeclarationResponseDTO {
    private Long id;
    private DeclarationType type;
    private LocalDateTime dateEvent;
    private String description;
    private String qrCode;
   // private List<String> imageUrls;
    private String itemName; // On peut aplatir l'objet pour le front
    private String color;
    private String brand;

    private DeclarationStatus status;
    private Long campusId;
    private String campusName;
    private Long locationId;
    private String locationName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dateCreated;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime lastModified;
}
