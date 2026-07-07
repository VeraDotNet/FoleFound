package com.veradotnet.folefound.users.domain.repository;

import com.veradotnet.folefound.users.application.enums.Role;
import com.veradotnet.folefound.users.domain.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<Users, Long> {
    Users findByUsername(String username);

    //Vérifie si le nom d'utilisateur est disponible
    boolean existsByUsername(String username);

    Page<Users> findAllByRoleNotAndIsActiveTrue(Role role, Pageable pageable);
}
