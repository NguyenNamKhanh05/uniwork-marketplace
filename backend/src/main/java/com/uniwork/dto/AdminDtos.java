package com.uniwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record UserAdminResponse(
            Long id,
            String email,
            String fullName,
            String role,
            String status,
            String authProvider,
            LocalDateTime createdAt,
            LocalDateTime lastLoginAt
    ) {
    }

    public record UserStatusRequest(
            @NotBlank(message = "Trạng thái không được để trống")
            String status,

            @Size(max = 500, message = "Lý do tối đa 500 ký tự")
            String reason
    ) {
    }

    public record AdminJobResponse(
            Long id,
            String title,
            String employerName,
            String categoryName,
            String status,
            Boolean hidden,
            Long applicationCount,
            Short positionsCount,
            Short positionsFilled,
            LocalDateTime createdAt
    ) {
    }

    public record JobVisibilityRequest(
            @NotNull(message = "Trạng thái hiển thị không được để trống")
            Boolean hidden,

            @Size(max = 500, message = "Lý do tối đa 500 ký tự")
            String reason
    ) {
    }

    public record AuditLogResponse(
            Long id,
            String actorName,
            String action,
            String entityType,
            Long entityId,
            String ipAddress,
            LocalDateTime createdAt
    ) {
    }

    public record DashboardResponse(
            long totalUsers,
            long totalStudents,
            long totalEmployers,
            long totalJobs,
            long openJobs,
            long totalApplications,
            long pendingApplications
    ) {
    }
}
