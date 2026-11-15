package com.uniwork.repository;

import com.uniwork.entity.StudentResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentResumeRepository extends JpaRepository<StudentResume, Long> {

    List<StudentResume> findByStudentUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long studentId);

    Optional<StudentResume> findByIdAndStudentUserIdAndDeletedAtIsNull(Long id, Long studentId);

    java.util.List<StudentResume> findByStudentUserIdAndIsDefaultTrueAndDeletedAtIsNull(Long studentId);

    Optional<StudentResume> findFirstByStudentUserIdAndIsDefaultTrueAndDeletedAtIsNull(Long studentId);

    Optional<StudentResume> findFirstByStudentUserIdAndFileUrlAndDeletedAtIsNull(Long studentId, String fileUrl);
}
