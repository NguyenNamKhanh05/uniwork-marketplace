package com.uniwork.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "student_skills")
@IdClass(StudentSkill.StudentSkillId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSkill {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Id
    @Column(name = "skill_id")
    private Integer skillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;

    @Column(name = "level", nullable = false, length = 12)
    @Builder.Default
    private String level = "BEGINNER";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSkillId implements Serializable {
        private Long studentId;
        private Integer skillId;
    }
}
