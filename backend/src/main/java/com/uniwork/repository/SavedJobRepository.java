package com.uniwork.repository;

import com.uniwork.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, SavedJob.SavedJobId> {

    boolean existsByStudentIdAndJobPostId(Long studentId, Long jobPostId);

    @EntityGraph(attributePaths = {"jobPost", "jobPost.employer", "jobPost.category"})
    Page<SavedJob> findByStudentId(Long studentId, Pageable pageable);

    long deleteByStudentIdAndJobPostId(Long studentId, Long jobPostId);
}
