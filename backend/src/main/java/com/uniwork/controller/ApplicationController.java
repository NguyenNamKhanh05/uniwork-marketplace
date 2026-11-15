package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.ApplicationDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API nộp đơn ứng tuyển và quản lý đơn ứng tuyển.
 */
@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(ApiEndpoints.V1_PUBLIC_JOBS + "/{jobId}/applications")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponse>> apply(
            Authentication authentication,
            @PathVariable Long jobId,
            @Valid @RequestBody ApplicationDtos.ApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Nộp đơn ứng tuyển thành công",
                applicationService.apply(AuthUtils.currentUser(authentication), jobId, request)));
    }

    @GetMapping(ApiEndpoints.V1_STUDENT + "/applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationDtos.ApplicationResponse>>> studentApplications(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn ứng tuyển thành công",
                applicationService.listStudentApplications(AuthUtils.currentUser(authentication), status, page, size)));
    }

    @GetMapping(ApiEndpoints.V1_STUDENT + "/applications/{id}")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponse>> studentApplicationDetail(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết đơn ứng tuyển thành công",
                applicationService.getStudentApplication(AuthUtils.currentUser(authentication), id)));
    }

    @PatchMapping(ApiEndpoints.V1_STUDENT + "/applications/{id}/withdraw")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponse>> withdraw(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã rút đơn ứng tuyển",
                applicationService.withdraw(AuthUtils.currentUser(authentication), id)));
    }

    @GetMapping(ApiEndpoints.V1_EMPLOYER + "/jobs/{jobId}/applications")
    public ResponseEntity<ApiResponse<PageResponse<ApplicationDtos.ApplicationResponse>>> jobApplications(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách ứng viên thành công",
                applicationService.listJobApplications(AuthUtils.currentUser(authentication), jobId, status, page, size)));
    }

    @GetMapping(ApiEndpoints.V1_EMPLOYER + "/applications/{id}")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponse>> employerApplicationDetail(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết ứng viên thành công",
                applicationService.getEmployerApplication(AuthUtils.currentUser(authentication), id)));
    }

    @PatchMapping(ApiEndpoints.V1_EMPLOYER + "/applications/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationDtos.ApplicationResponse>> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDtos.StatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái đơn ứng tuyển thành công",
                applicationService.updateStatus(AuthUtils.currentUser(authentication), id, request)));
    }
}
