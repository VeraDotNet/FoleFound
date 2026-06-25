package com.veradotnet.folefound.declaration.domain.repository;

import com.veradotnet.folefound.declaration.domain.model.Declaration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclarationRepo extends JpaRepository<Declaration, Long> {
}
