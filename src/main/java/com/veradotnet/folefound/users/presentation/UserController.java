package com.veradotnet.folefound.users.presentation;

import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.domain.service.AuthService;
import com.veradotnet.folefound.users.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user")
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

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getStaff(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "dateCreated", direction = Sort.Direction.ASC)
            Pageable pageable){
        Page<UserDTO> staffPage = userService.getStaffMembers(pageable);
        return new ResponseEntity<>(staffPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getStaffById(@PathVariable Long id) throws ResourceNotFoundException {
        UserDTO userDTO = userService.getStaffById(id);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateStaffAccount(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO,
            @RequestParam Role role) throws ResourceNotFoundException {
        UserDTO updatedUser = userService.updateStaffAccount(id, userDTO, role);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) throws ResourceNotFoundException {
        userService.deleteStaffAccount(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
