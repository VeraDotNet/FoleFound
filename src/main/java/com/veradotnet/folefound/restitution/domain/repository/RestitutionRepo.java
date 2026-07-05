package com.veradotnet.folefound.restitution.domain.repository;

import com.veradotnet.folefound.restitution.domain.model.Restitution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestitutionRepo extends JpaRepository<Restitution, Long> {
}
