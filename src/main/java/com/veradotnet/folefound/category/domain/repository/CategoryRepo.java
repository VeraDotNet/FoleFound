package com.veradotnet.folefound.category.domain.repository;

import com.veradotnet.folefound.category.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
