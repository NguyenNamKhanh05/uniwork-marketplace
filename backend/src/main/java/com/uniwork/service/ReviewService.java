package com.uniwork.service;

import com.uniwork.dto.ReviewDtos;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.entity.*;
import com.uniwork.exception.AppException;
import com.uniwork.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserDisplayService userDisplayService;

    @Transactional
    public ReviewDtos.ReviewResponse create(User reviewer, ReviewDtos.ReviewRequest request) {
        Application application = applicationRepository.findById(request.applicationId())
                .orElseThrow(() -> new AppException("Không tìm thấy đơn ứng tuyển", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        JobPost job = application.getJobPost();
        WorkAssignment assignment = workAssignmentRepository.findByApplicationId(application.getId())
                .orElseThrow(() -> new AppException("Đơn ứng tuyển chưa được chấp nhận", HttpStatus.CONFLICT, "REVIEW_NOT_ALLOWED"));

        boolean isStudent = application.getStudent().getUserId().equals(reviewer.getId());
        boolean isEmployer = job.getEmployer().getUserId().equals(reviewer.getId());
        if (!isStudent && !isEmployer) {
            throw new AppException("Bạn không có quyền đánh giá đơn ứng tuyển này", HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
        if (!"COMPLETED".equals(job.getStatus()) || !"COMPLETED".equals(assignment.getStatus())) {
            throw new AppException("Chỉ có thể đánh giá khi công việc đã hoàn thành", HttpStatus.CONFLICT, "REVIEW_NOT_ALLOWED");
        }
        if (reviewRepository.existsByAssignmentIdAndReviewerId(assignment.getId(), reviewer.getId())) {
            throw new AppException("Bạn đã đánh giá cho đơn ứng tuyển này rồi", HttpStatus.CONFLICT, "DUPLICATE_REVIEW");
        }

        User reviewee = isStudent ? job.getEmployer().getUser() : application.getStudent().getUser();
        if (reviewee.getId().equals(reviewer.getId())) {
            throw new AppException("Không thể tự đánh giá bản thân", HttpStatus.BAD_REQUEST, "REVIEW_NOT_ALLOWED");
        }

        Review review = reviewRepository.save(Review.builder()
                .assignment(assignment)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .reviewerRole(isStudent ? "STUDENT" : "EMPLOYER")
                .rating(request.rating().shortValue())
                .comment(request.comment())
                .isVisible(true)
                .build());

        notificationService.create(
                reviewee,
                "REVIEW_RECEIVED",
                "Bạn nhận được đánh giá mới",
                userDisplayService.displayName(reviewer) + " đã đánh giá " + request.rating() + " sao cho công việc \"" + job.getTitle() + "\".",
                "REVIEW",
                review.getId()
        );
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDtos.ReviewResponse> list(Long userId, Long jobId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> result;
        if (userId != null) {
            result = reviewRepository.findByRevieweeIdAndIsVisibleTrue(userId, pageable);
        } else if (jobId != null) {
            result = reviewRepository.findByAssignmentJobPostIdAndIsVisibleTrue(jobId, pageable);
        } else {
            Specification<Review> specification = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isTrue(root.get("isVisible")));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            result = reviewRepository.findAll(specification, pageable);
        }
        return PageResponse.of(result.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<ReviewDtos.PendingReviewResponse> pending(User user) {
        List<WorkAssignment> assignments = new ArrayList<>();
        assignments.addAll(workAssignmentRepository.findByStudentUserIdAndStatus(user.getId(), "COMPLETED"));
        assignments.addAll(workAssignmentRepository.findByEmployerUserIdAndStatus(user.getId(), "COMPLETED"));
        List<ReviewDtos.PendingReviewResponse> result = new ArrayList<>();
        for (WorkAssignment assignment : assignments) {
            if (reviewRepository.existsByAssignmentIdAndReviewerId(assignment.getId(), user.getId())) {
                continue;
            }
            boolean isStudent = assignment.getStudent().getUserId().equals(user.getId());
            User counterpart = isStudent
                    ? assignment.getEmployer().getUser()
                    : assignment.getStudent().getUser();
            result.add(new ReviewDtos.PendingReviewResponse(
                    assignment.getId(),
                    assignment.getApplication().getId(),
                    assignment.getJobPost().getId(),
                    assignment.getJobPost().getTitle(),
                    userDisplayService.displayName(counterpart),
                    isStudent ? "EMPLOYER" : "STUDENT",
                    isStudent ? "STUDENT" : "EMPLOYER"
            ));
        }
        return result;
    }

    private ReviewDtos.ReviewResponse toResponse(Review review) {
        WorkAssignment assignment = review.getAssignment();
        return new ReviewDtos.ReviewResponse(
                review.getId(),
                assignment != null && assignment.getApplication() != null ? assignment.getApplication().getId() : null,
                assignment != null && assignment.getJobPost() != null ? assignment.getJobPost().getId() : null,
                assignment != null && assignment.getJobPost() != null ? assignment.getJobPost().getTitle() : null,
                review.getReviewer().getId(),
                userDisplayService.displayName(review.getReviewer()),
                review.getReviewerRole(),
                review.getReviewee().getId(),
                userDisplayService.displayName(review.getReviewee()),
                review.getRating() == null ? null : review.getRating().intValue(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private int clampSize(int size) {
        return size <= 0 ? 10 : Math.min(size, 50);
    }
}
