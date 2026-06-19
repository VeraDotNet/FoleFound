package com.veradotnet.folefound.campus.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class CampusDTO {
    private Long id;
    private String name;
    private LocalDateTime dateCreated;
    private LocalDateTime lastModified;
}
