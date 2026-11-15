package com.uniwork.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "job_post_skills")
@IdClass(JobPostSkill.JobPostSkillId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostSkill {

    @Id
    @Column(name = "job_post_id")
    private Long jobPostId;

    @Id
    @Column(name = "skill_id")
    private Integer skillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", insertable = false, updatable = false)
    private Skill skill;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPostSkillId implements Serializable {
        private Long jobPostId;
        private Integer skillId;
    }
}
