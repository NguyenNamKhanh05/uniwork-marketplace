package com.uniwork.repository;

import com.uniwork.entity.StudentSkill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkill.StudentSkillId> {

    @EntityGraph(attributePaths = {"skill"})
    List<StudentSkill> findByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);
}
