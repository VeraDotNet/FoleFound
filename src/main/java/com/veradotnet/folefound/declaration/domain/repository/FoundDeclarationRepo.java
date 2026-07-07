package com.veradotnet.folefound.declaration.domain.repository;

import com.veradotnet.folefound.declaration.domain.model.FoundDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoundDeclarationRepo extends JpaRepository<FoundDeclaration, Long> {
}
