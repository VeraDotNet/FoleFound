package com.veradotnet.folefound.matching.domain.repository;

import com.veradotnet.folefound.matching.application.enums.MatchingStatus;
import com.veradotnet.folefound.matching.domain.model.Matching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingRepo extends JpaRepository<Matching, Long> {
    Page<Matching> findAllByStatus(MatchingStatus status, Pageable pageable);

    long countByStatus(MatchingStatus status);

}
