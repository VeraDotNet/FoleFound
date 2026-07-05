package com.veradotnet.folefound.campus.domain.repository;

import com.veradotnet.folefound.campus.domain.model.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusRepo extends JpaRepository<Campus, Long> {
    boolean existsByNameIgnoreCase(String name);
}
