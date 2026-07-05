package com.veradotnet.folefound.item.domain.repository;

import com.veradotnet.folefound.item.domain.Model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepo extends JpaRepository<Item, Long> {
    boolean existsByCategoryId(Long categoryId);
}

