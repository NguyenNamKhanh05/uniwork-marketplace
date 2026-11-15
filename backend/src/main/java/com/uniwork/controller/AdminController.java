package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.AdminDtos;
import com.uniwork.dto.JobDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.AdminService;
import com.uniwork.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API quản trị hệ thống.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.V1_ADMIN)
public class AdminController {

    private final AdminService adminService;
    private final JobService jobService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDtos.DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy số liệu tổng quan thành công",
                adminService.dashboard()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<AdminDtos.UserAdminResponse>>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách người dùng thành công",
                adminService.listUsers(keyword, role, status, page, size)));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<AdminDtos.UserAdminResponse>> updateUserStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.UserStatusRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái người dùng thành công",
                adminService.updateUserStatus(
                        AuthUtils.currentUser(authentication), id, request, clientIp(servletRequest))));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<PageResponse<AdminDtos.AdminJobResponse>>> jobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean hidden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tin tuyển dụng thành công",
                jobService.adminJobs(keyword, status, hidden, page, size)));
    }

    @PatchMapping("/jobs/{id}/visibility")
    public ResponseEntity<ApiResponse<Void>> jobVisibility(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.JobVisibilityRequest request,
            HttpServletRequest servletRequest) {
        jobService.setVisibility(AuthUtils.currentUser(authentication), id, request.hidden(), request.reason());
        return ResponseEntity.ok(ApiResponse.success(
                request.hidden() ? "Đã ẩn tin tuyển dụng" : "Đã hiển thị lại tin tuyển dụng",
                null));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PageResponse<AdminDtos.AuditLogResponse>>> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy nhật ký thao tác thành công",
                adminService.auditLogs(page, size)));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
