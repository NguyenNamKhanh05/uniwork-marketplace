package com.uniwork.repository;

import com.uniwork.entity.JobPostSkill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostSkillRepository extends JpaRepository<JobPostSkill, JobPostSkill.JobPostSkillId> {

    @EntityGraph(attributePaths = {"skill"})
    List<JobPostSkill> findByJobPostId(Long jobPostId);

    void deleteByJobPostId(Long jobPostId);
}
