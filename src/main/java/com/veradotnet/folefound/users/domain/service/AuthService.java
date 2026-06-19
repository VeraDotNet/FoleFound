package com.veradotnet.folefound.users.domain.service;

import com.veradotnet.folefound.preRegistration.application.AcademicStatus;
import com.veradotnet.folefound.preRegistration.domain.repository.PreRegistrationRepo;
import com.veradotnet.folefound.users.application.dto.LoginRequest;
import com.veradotnet.folefound.users.application.dto.UserDTO;
import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.application.mapper.StudentMapper;
import com.veradotnet.folefound.users.application.mapper.UserMapper;
//import com.veradotnet.folefound.users.application.mapper.UserProfileMapper;
import com.veradotnet.folefound.users.domain.model.Student;
//import com.veradotnet.folefound.users.domain.model.UserProfile;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.StudentRepo;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;

    private final StudentRepo studentRepo;

    private final PreRegistrationRepo preRegistrationRepo;

    private final AuthenticationManager authManager;

    private final JWTService jwtService;

    private final BCryptPasswordEncoder encoder;

    public UserDTO register(UserDTO userDTO) {

        // RM 0 :On vérifie si l'étudiant fait partie de la liste blanche de l'administration
        boolean isPreRegistered = preRegistrationRepo.existsByStudentCodeAndAcademicStatus(
                userDTO.getStudentCode(),
                AcademicStatus.Active
        );

        if (!isPreRegistered) {
            throw new IllegalArgumentException("Invalid student code");
        }

        // RM 1 : Vérifier si le matricule est déjà pris
        if (studentRepo.existsByStudentCode(userDTO.getStudentCode()))
            throw new IllegalArgumentException("This student ID already taken");

        // RM 2 : Vérifier si le username de connexion est déjà pris
        if (userRepo.existsByUsername(userDTO.getUsername()))
            throw new IllegalArgumentException("This username is already taken");

        Student studentToSave = StudentMapper.INSTANCE.toModel(userDTO);

        studentToSave.setPassword(encoder.encode(userDTO.getPassword()));
        studentToSave.setRole(Role.ROLE_STUDENT);
        studentToSave.setIsActive(true);

        Student persistedStudent = studentRepo.save(studentToSave);
        //return in dto
        return StudentMapper.INSTANCE.toDTO(persistedStudent);
    }

    public String verify(LoginRequest loginRequest) {
        try {
            // Spring Security manager vérifies username, password
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );
            if (authentication.isAuthenticated()) {
                Users user = userRepo.findByUsername(loginRequest.getUsername());
                return jwtService.generateToken(loginRequest.getUsername(), user);
            }
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mot de passe incorrect");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur d'authentification : " + e.getMessage());
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Échec authentification");
    }
}