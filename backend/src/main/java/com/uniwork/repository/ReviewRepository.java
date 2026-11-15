package com.uniwork.repository;

import com.uniwork.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    boolean existsByAssignmentIdAndReviewerId(Long assignmentId, Long reviewerId);

    @EntityGraph(attributePaths = {"assignment", "assignment.jobPost", "reviewer", "reviewee"})
    Page<Review> findByRevieweeIdAndIsVisibleTrue(Long revieweeId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment", "assignment.jobPost", "reviewer", "reviewee"})
    Page<Review> findByAssignmentJobPostIdAndIsVisibleTrue(Long jobPostId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignment", "assignment.jobPost", "reviewer", "reviewee"})
    List<Review> findByAssignmentIdInAndReviewerId(List<Long> assignmentIds, Long reviewerId);

    long countByRevieweeIdAndIsVisibleTrue(Long revieweeId);
}
