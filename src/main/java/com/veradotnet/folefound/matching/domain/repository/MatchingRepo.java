package com.veradotnet.folefound.matching.domain.repository;

import com.veradotnet.folefound.matching.domain.model.Matching;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingRepo extends JpaRepository<Matching, Long> {
}
