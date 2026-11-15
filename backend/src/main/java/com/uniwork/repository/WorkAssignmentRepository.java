package com.uniwork.repository;

import com.uniwork.entity.WorkAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkAssignmentRepository extends JpaRepository<WorkAssignment, Long> {

    Optional<WorkAssignment> findByApplicationId(Long applicationId);

    @EntityGraph(attributePaths = {"application", "jobPost", "jobPost.employer", "student", "student.user", "employer"})
    Optional<WorkAssignment> findByIdAndStudentUserId(Long id, Long studentId);

    @EntityGraph(attributePaths = {"application", "jobPost", "student", "student.user", "employer"})
    List<WorkAssignment> findByJobPostId(Long jobPostId);

    @EntityGraph(attributePaths = {"application", "jobPost", "jobPost.employer", "employer"})
    List<WorkAssignment> findByStudentUserIdAndStatus(Long studentId, String status);

    @EntityGraph(attributePaths = {"application", "jobPost", "student", "student.user", "employer"})
    List<WorkAssignment> findByEmployerUserIdAndStatus(Long employerId, String status);

    long countByStatus(String status);
}
