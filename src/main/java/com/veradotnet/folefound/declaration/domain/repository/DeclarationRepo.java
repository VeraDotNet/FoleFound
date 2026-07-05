package com.veradotnet.folefound.declaration.domain.repository;

import com.veradotnet.folefound.declaration.application.enums.DeclarationStatus;
import com.veradotnet.folefound.declaration.application.enums.DeclarationType;
import com.veradotnet.folefound.declaration.domain.model.Declaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeclarationRepo extends JpaRepository<Declaration, Long> {

    List<Declaration> findByTypeAndStatus(DeclarationType type, DeclarationStatus status);

    List<Declaration> findByTypeAndStatusAndItemCategoryId(
            DeclarationType type,
            DeclarationStatus status,
            Long categoryId);

    Page<Declaration> findByUserId(Long userId, Pageable pageable);
}
