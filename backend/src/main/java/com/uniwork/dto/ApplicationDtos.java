package com.uniwork.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    public record ApplyRequest(
            @Size(max = 2000, message = "Thư giới thiệu tối đa 2000 ký tự")
            String coverLetter,

            @Pattern(regexp = "^$|^https?://.+", message = "Liên kết CV phải bắt đầu bằng http:// hoặc https://")
            @Size(max = 500, message = "Liên kết CV tối đa 500 ký tự")
            String cvUrl,

            Long resumeId,

            @DecimalMin(value = "0", message = "Mức lương mong muốn không được âm")
            BigDecimal expectedSalary
    ) {
    }

    public record StatusUpdateRequest(
            @NotBlank(message = "Trạng thái không được để trống")
            String status,

            @Size(max = 500, message = "Lý do tối đa 500 ký tự")
            String reason
    ) {
    }

    public record HistoryItem(
            String fromStatus,
            String toStatus,
            String note,
            String changedByName,
            LocalDateTime changedAt
    ) {
    }

    public record ApplicationResponse(
            Long id,
            Long jobId,
            String jobTitle,
            String jobStatus,
            String workMode,
            Short provinceId,
            String provinceName,
            String addressDetail,
            String salaryType,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            LocalDate startDate,
            LocalDateTime applicationDeadline,
            Long employerId,
            String employerName,
            String employerLogoUrl,
            BigDecimal employerRating,
            Integer employerRatingCount,
            Long studentId,
            String studentName,
            String studentEmail,
            String studentPhone,
            String university,
            String major,
            Short yearOfStudy,
            String studentBio,
            String avatarUrl,
            String coverLetter,
            String cvUrl,
            String cvTitle,
            BigDecimal expectedSalary,
            String status,
            String employerNote,
            String rejectReason,
            LocalDateTime appliedAt,
            LocalDateTime reviewedAt,
            LocalDateTime decidedAt,
            Long assignmentId,
            String assignmentStatus,
            Boolean reviewed,
            List<HistoryItem> history
    ) {
    }
}
