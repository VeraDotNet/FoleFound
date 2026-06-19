package com.veradotnet.folefound.preRegistration.domain.model;

import com.veradotnet.folefound.preRegistration.application.AcademicStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pre_registration", indexes = {
        @Index(name = "idx_prereg_code_status", columnList = "studentCode, academicStatus")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String studentCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcademicStatus academicStatus;
}
