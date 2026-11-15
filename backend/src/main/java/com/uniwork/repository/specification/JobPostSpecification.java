package com.uniwork.repository.specification;

import com.uniwork.entity.JobPost;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobPostSpecification {

    public static Specification<JobPost> filterPublicJobs(
            String keyword,
            Short categoryId,
            Short provinceId,
            String workMode,
            String salaryType,
            BigDecimal minSalary,
            BigDecimal maxSalary
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), "OPEN"));
            predicates.add(cb.or(
                    cb.isNull(root.get("applicationDeadline")),
                    cb.greaterThanOrEqualTo(root.get("applicationDeadline"), LocalDateTime.now())
            ));
            predicates.add(cb.equal(root.get("employer").get("user").get("status"), "ACTIVE"));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleLike, descLike));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (provinceId != null) {
                predicates.add(cb.equal(root.get("provinceId"), provinceId));
            }
            if (workMode != null && !workMode.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("workMode"), workMode.trim()));
            }
            if (salaryType != null && !salaryType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("salaryType"), salaryType.trim()));
            }
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), minSalary));
            }
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMin"), maxSalary));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<JobPost> filterAdminJobs(String keyword, String status, Boolean hidden) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (hidden != null) {
                predicates.add(hidden ? cb.isNotNull(root.get("deletedAt")) : cb.isNull(root.get("deletedAt")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
