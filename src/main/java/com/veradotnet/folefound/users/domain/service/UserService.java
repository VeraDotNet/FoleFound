package com.veradotnet.folefound.users.domain.service;

import com.veradotnet.folefound.shared.exception.ResourceInUseException;
import com.veradotnet.folefound.shared.exception.ResourceNotFoundException;
import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.application.mapper.UserMapper;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepo userRepo;

    private final BCryptPasswordEncoder encoder;

    @Transactional
    public UserDTO createAccountByAdmin(UserDTO userDTO, Role chosenRole) {
        try {
            // Convertir le DTO en entité Users
            Users user = UserMapper.INSTANCE.toModel(userDTO);

            user.setPassword(encoder.encode(userDTO.getPassword()));

            user.setRole(chosenRole);
            user.setIsActive(true);

            if (chosenRole == Role.ROLE_STUDENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student cannot be created here");
            }

            //Save
            Users savedUser = userRepo.save(user);
            return UserMapper.INSTANCE.toDTO(savedUser);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to create user");
        }
    }

    public Page<UserDTO> getStaffMembers(Pageable pageable) {
        // On appelle la méthode custom du repository
        Page<Users> staffPage = userRepo.findAllByRoleNotAndIsActiveTrue(Role.ROLE_STUDENT, pageable);

        // On convertit le résultat en DTO pour le contrôleur
        return staffPage.map(UserMapper.INSTANCE::toDTO);
    }

    public UserDTO getStaffById(Long id) throws ResourceNotFoundException {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));

        // Sécurité : On s'assure qu'un admin ne détourne pas cette route pour lire un étudiant
        if (user.getRole() == Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("Error: This user is not a staff member.");
        }

        return UserMapper.INSTANCE.toDTO(user);
    }

    @Transactional
    public UserDTO updateStaffAccount(Long id, UserDTO userDTO, Role newRole) throws ResourceNotFoundException {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Mise à jour des informations de base
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        // Si le rôle a changé et qu'il est valide
        if (newRole != null && newRole != Role.ROLE_STUDENT) {
            user.setRole(newRole);
        } else if (newRole == Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("Cannot set to Student role");
        }

        // Si un nouveau mot de passe est fourni (optionnel lors d'une modification)
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(encoder.encode(userDTO.getPassword()));
        }

        Users updatedUser = userRepo.save(user);
        return UserMapper.INSTANCE.toDTO(updatedUser);
    }

    @Transactional
    public void deleteStaffAccount(Long id) throws ResourceNotFoundException {
        Users user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);

        userRepo.save(user);
    }
}
