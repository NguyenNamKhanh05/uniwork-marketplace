package com.uniwork.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class JobDtos {

    private JobDtos() {
    }

    public record JobRequest(
            @NotBlank(message = "Tiêu đề không được để trống")
            @Size(min = 5, max = 200, message = "Tiêu đề phải từ 5 đến 200 ký tự")
            String title,

            @NotNull(message = "Danh mục không được để trống")
            Short categoryId,

            @NotBlank(message = "Mô tả không được để trống")
            @Size(min = 10, max = 5000, message = "Mô tả phải từ 10 đến 5000 ký tự")
            String description,

            @Size(max = 5000, message = "Yêu cầu tối đa 5000 ký tự")
            String requirements,

            @Size(max = 5000, message = "Quyền lợi tối đa 5000 ký tự")
            String benefits,

            @NotBlank(message = "Hình thức làm việc không được để trống")
            @Pattern(regexp = "ONSITE|REMOTE|HYBRID", message = "Hình thức làm việc không hợp lệ")
            String workMode,

            Short provinceId,
            Integer districtId,

            @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
            String addressDetail,

            @NotBlank(message = "Hình thức trả lương không được để trống")
            @Pattern(regexp = "HOURLY|DAILY|WEEKLY|MONTHLY|PER_TASK|PER_SHIFT|PER_JOB|NEGOTIABLE",
                    message = "Hình thức trả lương không hợp lệ")
            String salaryType,

            @DecimalMin(value = "0", message = "Lương tối thiểu không được âm")
            BigDecimal salaryMin,

            @DecimalMin(value = "0", message = "Lương tối đa không được âm")
            BigDecimal salaryMax,

            @NotNull(message = "Số lượng tuyển không được để trống")
            @Min(value = 1, message = "Số lượng tuyển tối thiểu là 1")
            @Max(value = 100, message = "Số lượng tuyển tối đa là 100")
            Short positionsCount,

            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime applicationDeadline,

            List<Integer> skillIds,

            @Valid
            List<ScheduleRequest> schedules
    ) {
    }

    public record ScheduleRequest(
            @NotNull(message = "Thứ trong tuần không được để trống")
            @Min(value = 1, message = "Thứ phải từ 1 đến 7")
            @Max(value = 7, message = "Thứ phải từ 1 đến 7")
            Short dayOfWeek,

            @NotNull(message = "Giờ bắt đầu không được để trống")
            LocalTime startTime,

            @NotNull(message = "Giờ kết thúc không được để trống")
            LocalTime endTime
    ) {
    }

    public record SkillItem(Long id, String name, String level) {
    }

    public record ScheduleItem(Short dayOfWeek, LocalTime startTime, LocalTime endTime) {
    }

    public record JobResponse(
            Long id,
            String title,
            Short categoryId,
            String categoryName,
            String description,
            String requirements,
            String benefits,
            String workMode,
            Short provinceId,
            String provinceName,
            Integer districtId,
            String districtName,
            String addressDetail,
            String salaryType,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String salaryCurrency,
            Short positionsCount,
            Short positionsFilled,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime applicationDeadline,
            String status,
            Integer viewCount,
            LocalDateTime publishedAt,
            LocalDateTime createdAt,
            Long employerId,
            String employerName,
            Boolean employerVerified,
            BigDecimal employerRating,
            Integer employerRatingCount,
            String employerLogoUrl,
            String employerDescription,
            Long applicationCount,
            Boolean saved,
            List<SkillItem> skills,
            List<ScheduleItem> schedules
    ) {
    }

    public record CategoryResponse(
            Short id,
            String name,
            String slug,
            String description,
            Short sortOrder,
            Boolean isActive,
            Short parentId
    ) {
    }

    public record CategoryRequest(
            @NotBlank(message = "Tên danh mục không được để trống")
            @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
            String name,

            @Size(max = 120, message = "Slug tối đa 120 ký tự")
            String slug,

            @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
            String description,

            Short sortOrder,
            Short parentId,
            Boolean isActive
    ) {
    }

    public record VisibilityRequest(
            @NotNull(message = "Trạng thái hiển thị không được để trống")
            Boolean visible
    ) {
    }

    public record ProvinceResponse(Short id, String code, String name) {
    }

    public record DistrictResponse(Integer id, Short provinceId, String code, String name) {
    }

    public record SkillResponse(Integer id, String name, String slug) {
    }
}
