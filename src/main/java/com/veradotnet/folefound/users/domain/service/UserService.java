package com.veradotnet.folefound.users.domain.service;

import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.application.mapper.UserMapper;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.StudentRepo;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;

    private final BCryptPasswordEncoder encoder;

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
}
