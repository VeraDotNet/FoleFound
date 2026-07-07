package com.veradotnet.folefound.location.domain.repository;

import com.veradotnet.folefound.campus.domain.model.Campus;
import com.veradotnet.folefound.location.domain.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepo extends JpaRepository<Location, Long> {

    Boolean existsByCampusId(Long campusId);

    boolean existsByNameIgnoreCase(String name);

    Page<Location> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
