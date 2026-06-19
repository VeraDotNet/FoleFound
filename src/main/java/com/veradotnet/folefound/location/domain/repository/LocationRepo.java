package com.veradotnet.folefound.location.domain.repository;

import com.veradotnet.folefound.location.domain.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepo extends JpaRepository<Location, Long> {
}
