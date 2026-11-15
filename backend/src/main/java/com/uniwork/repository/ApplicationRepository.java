package com.uniwork.repository;

import com.uniwork.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByJobPostIdAndStudentUserId(Long jobPostId, Long studentId);

    @EntityGraph(attributePaths = {"jobPost", "jobPost.employer", "jobPost.category", "student", "resume"})
    Optional<Application> findByIdAndStudentUserId(Long id, Long studentId);

    @EntityGraph(attributePaths = {"jobPost", "jobPost.employer", "jobPost.category", "student", "resume"})
    Optional<Application> findByIdAndJobPostEmployerUserId(Long id, Long employerId);

    @EntityGraph(attributePaths = {"jobPost", "jobPost.employer", "jobPost.category", "resume"})
    Page<Application> findByStudentUserId(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"jobPost", "jobPost.employer", "jobPost.category", "resume"})
    Page<Application> findByStudentUserIdAndStatus(Long studentId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "resume", "jobPost"})
    Page<Application> findByJobPostId(Long jobPostId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "resume", "jobPost"})
    Page<Application> findByJobPostIdAndStatus(Long jobPostId, String status, Pageable pageable);

    long countByJobPostId(Long jobPostId);

    long countByJobPostIdAndStatus(Long jobPostId, String status);

    long countByStudentUserId(Long studentId);

    long countByStatus(String status);

    long countByStudentUserIdAndStatus(Long studentId, String status);
}
