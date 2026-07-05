package com.veradotnet.folefound.declaration.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeclarationRequestDTO {
    // ---- Infos pour l'objet (Item) ----
    @NotBlank(message = "The name of the object is required")
    private String itemName;

    @NotBlank(message = "The color is required")
    private String color;

    private String brand;

    @NotNull(message = "The category is required")
    private Long categoryId;

    // ---- Infos pour la Déclaration ----
    @NotNull(message = "Le type de déclaration (PERTE/TROUVE) est obligatoire")
    private DeclarationType type;

    @NotNull(message = "The event date is required")
    private LocalDateTime dateEvent;

    private String description;

    @NotNull(message = "The place is required")
    private Long locationId;
}
