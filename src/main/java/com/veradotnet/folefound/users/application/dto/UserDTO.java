package com.veradotnet.folefound.users.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.veradotnet.folefound.users.application.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class UserDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "Username required")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password required")
    private String password;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Role role;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean isActive;

    private String firstName;
    private String lastName;

    @NotBlank(message = "Email required")
    @Email(message = "Invalid email format")
    private String email;

    //private UserProfileDTO userProfileDTO;
}
