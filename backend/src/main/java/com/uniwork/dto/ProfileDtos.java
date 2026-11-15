package com.uniwork.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record StudentSkillRequest(
            @NotNull(message = "Kỹ năng không được để trống")
            Integer skillId,

            @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED", message = "Mức độ kỹ năng không hợp lệ")
            String level
    ) {
    }

    public record UpdateProfileRequest(
            @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
            String fullName,

            @Pattern(regexp = "^$|^0[0-9]{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0")
            String phone,

            LocalDate dateOfBirth,

            @Pattern(regexp = "^$|MALE|FEMALE|OTHER", message = "Giới tính không hợp lệ")
            String gender,

            @Size(max = 20, message = "Mã sinh viên tối đa 20 ký tự")
            String studentCode,

            @Size(max = 150, message = "Tên trường tối đa 150 ký tự")
            String university,

            @Size(max = 100, message = "Chuyên ngành tối đa 100 ký tự")
            String major,

            @Min(value = 1, message = "Năm học phải từ 1 đến 8")
            @Max(value = 8, message = "Năm học phải từ 1 đến 8")
            Short yearOfStudy,

            Short provinceId,

            @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
            String address,

            @Size(max = 2000, message = "Giới thiệu tối đa 2000 ký tự")
            String bio,

            @Size(max = 500, message = "Ảnh đại diện tối đa 500 ký tự")
            String avatarUrl,

            @Pattern(regexp = "^$|COMPANY|INDIVIDUAL", message = "Loại nhà tuyển dụng không hợp lệ")
            String employerType,

            @Size(max = 150, message = "Tên đơn vị tối đa 150 ký tự")
            String displayName,

            @Size(max = 100, message = "Người liên hệ tối đa 100 ký tự")
            String contactName,

            @Pattern(regexp = "^$|^0[0-9]{9}$", message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0")
            String contactPhone,

            @Size(max = 20, message = "Mã số thuế tối đa 20 ký tự")
            String taxCode,

            @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
            String description,

            @Size(max = 500, message = "Logo tối đa 500 ký tự")
            String logoUrl,

            @Size(max = 255, message = "Website tối đa 255 ký tự")
            String website,

            @Valid
            List<StudentSkillRequest> skills
    ) {
    }

    public record ProfileResponse(
            Long userId,
            String email,
            String role,
            String fullName,
            String phone,
            LocalDate dateOfBirth,
            String gender,
            String studentCode,
            String university,
            String major,
            Short yearOfStudy,
            Short provinceId,
            String provinceName,
            String address,
            String bio,
            String avatarUrl,
            BigDecimal avgRating,
            Integer ratingCount,
            String employerType,
            String displayName,
            String contactName,
            String contactPhone,
            String taxCode,
            String description,
            String logoUrl,
            String website,
            Boolean isVerified,
            LocalDateTime verifiedAt,
            List<JobDtos.SkillItem> skills,
            List<ResumeResponse> resumes
    ) {
    }

    public record ResumeRequest(
            @NotBlank(message = "Tiêu đề CV không được để trống")
            @Size(max = 150, message = "Tiêu đề CV tối đa 150 ký tự")
            String title,

            @NotBlank(message = "Liên kết CV không được để trống")
            @Pattern(regexp = "^https?://.+", message = "Liên kết CV phải bắt đầu bằng http:// hoặc https://")
            @Size(max = 500, message = "Liên kết CV tối đa 500 ký tự")
            String fileUrl,

            @Size(max = 255, message = "Tên tệp tối đa 255 ký tự")
            String fileName,

            Boolean isDefault
    ) {
    }

    public record ResumeResponse(
            Long id,
            String title,
            String fileUrl,
            String fileName,
            Boolean isDefault,
            LocalDateTime createdAt
    ) {
    }
}
