package com.veradotnet.folefound.users.presentation;

import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.domain.service.AuthService;
import com.veradotnet.folefound.users.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserDTO> createStaffAccount(
            @Valid @RequestBody UserDTO userDTO,
            @RequestParam Role role) {

        UserDTO createdUser = userService.createAccountByAdmin(userDTO, role);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}
