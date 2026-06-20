package com.veradotnet.folefound.preRegistration.domain.repository;

import com.veradotnet.folefound.preRegistration.application.AcademicStatus;
import com.veradotnet.folefound.preRegistration.domain.model.PreRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreRegistrationRepo extends JpaRepository<PreRegistration, Long> {
    // Vérifie si le matricule existe avec le statut ACTIF
    boolean existsByStudentCodeAndAcademicStatus(String studentCode, AcademicStatus status);
}
