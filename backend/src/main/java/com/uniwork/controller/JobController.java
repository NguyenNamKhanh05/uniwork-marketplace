package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.JobDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * API tin tuyển dụng công khai và API quản lý tin của nhà tuyển dụng.
 */
@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping(ApiEndpoints.V1_PUBLIC_JOBS)
    public ResponseEntity<ApiResponse<PageResponse<JobDtos.JobResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Short categoryId,
            @RequestParam(required = false) Short provinceId,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String salaryType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách việc làm thành công",
                jobService.search(keyword, categoryId, provinceId, workMode, salaryType,
                        minSalary, maxSalary, page, size, sort)));
    }

    @GetMapping(ApiEndpoints.V1_PUBLIC_JOBS + "/{id}")
    public ResponseEntity<ApiResponse<JobDtos.JobResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết việc làm thành công",
                jobService.getPublicDetail(id)));
    }

    @GetMapping(ApiEndpoints.V1_EMPLOYER + "/jobs")
    public ResponseEntity<ApiResponse<PageResponse<JobDtos.JobResponse>>> myJobs(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tin tuyển dụng thành công",
                jobService.listEmployerJobs(AuthUtils.currentUser(authentication), status, page, size)));
    }

    @GetMapping(ApiEndpoints.V1_EMPLOYER + "/jobs/{id}")
    public ResponseEntity<ApiResponse<JobDtos.JobResponse>> myJobDetail(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy chi tiết tin tuyển dụng thành công",
                jobService.getEmployerJob(AuthUtils.currentUser(authentication), id)));
    }

    @PostMapping(ApiEndpoints.V1_EMPLOYER + "/jobs")
    public ResponseEntity<ApiResponse<JobDtos.JobResponse>> create(
            Authentication authentication,
            @Valid @RequestBody JobDtos.JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Đăng tin tuyển dụng thành công",
                jobService.createJob(AuthUtils.currentUser(authentication), request)));
    }

    @PutMapping(ApiEndpoints.V1_EMPLOYER + "/jobs/{id}")
    public ResponseEntity<ApiResponse<JobDtos.JobResponse>> update(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody JobDtos.JobRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật tin tuyển dụng thành công",
                jobService.updateJob(AuthUtils.currentUser(authentication), id, request)));
    }

    @PatchMapping(ApiEndpoints.V1_EMPLOYER + "/jobs/{id}/close")
    public ResponseEntity<ApiResponse<JobDtos.JobResponse>> close(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đóng tin tuyển dụng",
                jobService.closeJob(AuthUtils.currentUser(authentication), id)));
    }

    @PatchMapping(ApiEndpoints.V1_EMPLOYER + "/jobs/{id}/status")
    public ResponseEntity<ApiResponse<JobDtos.JobResponse>> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái tin tuyển dụng thành công",
                jobService.updateJobStatus(AuthUtils.currentUser(authentication), id, status.toUpperCase())));
    }
}
