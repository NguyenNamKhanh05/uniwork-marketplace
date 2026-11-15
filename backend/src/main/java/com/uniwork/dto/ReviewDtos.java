package com.uniwork.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record ReviewRequest(
            @NotNull(message = "Đơn ứng tuyển không được để trống")
            Long applicationId,

            @NotNull(message = "Điểm đánh giá không được để trống")
            @Min(value = 1, message = "Điểm đánh giá từ 1 đến 5")
            @Max(value = 5, message = "Điểm đánh giá từ 1 đến 5")
            Integer rating,

            @Size(max = 1000, message = "Nhận xét tối đa 1000 ký tự")
            String comment
    ) {
    }

    public record ReviewResponse(
            Long id,
            Long applicationId,
            Long jobId,
            String jobTitle,
            Long reviewerId,
            String reviewerName,
            String reviewerRole,
            Long revieweeId,
            String revieweeName,
            Integer rating,
            String comment,
            LocalDateTime createdAt
    ) {
    }

    public record PendingReviewResponse(
            Long assignmentId,
            Long applicationId,
            Long jobId,
            String jobTitle,
            String counterpartName,
            String counterpartRole,
            String reviewerRole
    ) {
    }

    public record ReviewListResponse(
            List<ReviewResponse> content,
            long totalElements,
            int totalPages,
            int page,
            int size
    ) {
    }
}
