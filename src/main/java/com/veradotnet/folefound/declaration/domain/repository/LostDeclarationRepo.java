package com.veradotnet.folefound.declaration.domain.repository;

import com.veradotnet.folefound.declaration.domain.model.LostDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostDeclarationRepo extends JpaRepository<LostDeclaration, Long> {
}
