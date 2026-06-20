package com.veradotnet.folefound.shared.config;

import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.domain.model.Users;
import com.veradotnet.folefound.users.domain.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

        @Bean
        CommandLineRunner initAdmin(UserRepo userRepo, PasswordEncoder encoder) {
            return args -> {
                if (!userRepo.existsByUsername("superadmin")) {
                    Users admin = Users.builder()
                            .username("superadmin")
                            .password(encoder.encode("Admin123")) // Hachage parfait via ton encoder
                            .email("admin@fole.com")
                            .role(Role.ROLE_ADMIN)
                            .isActive(true)
                            .firstName("Super")
                            .lastName("Admin")
                            .build();

                    userRepo.save(admin);
                    System.out.println(">> Compte Admin créé avec succès (superadmin / Admin@123) <<");
                }
            };
        }
}
