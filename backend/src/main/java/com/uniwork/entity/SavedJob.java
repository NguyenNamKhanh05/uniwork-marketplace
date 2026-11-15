package com.uniwork.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_jobs")
@IdClass(SavedJob.SavedJobId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedJob {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Id
    @Column(name = "job_post_id")
    private Long jobPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", insertable = false, updatable = false)
    private JobPost jobPost;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedJobId implements Serializable {
        private Long studentId;
        private Long jobPostId;
    }
}
