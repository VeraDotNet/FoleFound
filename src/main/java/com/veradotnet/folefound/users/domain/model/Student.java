package com.veradotnet.folefound.users.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_code", columnList = "studentCode")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Student extends Users{

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String studentCode;
}
