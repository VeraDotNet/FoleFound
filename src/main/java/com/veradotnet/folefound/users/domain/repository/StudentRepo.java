package com.veradotnet.folefound.users.domain.repository;

import com.veradotnet.folefound.users.domain.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepo extends JpaRepository<Student, Long> {
    boolean existsByStudentCode(String studentCode);
}
