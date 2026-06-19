package com.veradotnet.folefound.users.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class StudentDTO extends UserDTO{

    @NotBlank(message = "Student code required")
    private String studentCode;
}
