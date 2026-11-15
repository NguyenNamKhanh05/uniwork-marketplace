package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.ProfileDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API hồ sơ cá nhân của sinh viên và nhà tuyển dụng.
 */
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping(ApiEndpoints.V1_PROFILES + "/me")
    public ResponseEntity<ApiResponse<ProfileDtos.ProfileResponse>> myProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy hồ sơ thành công",
                profileService.getMyProfile(AuthUtils.currentUser(authentication))));
    }

    @PutMapping(ApiEndpoints.V1_PROFILES + "/me")
    public ResponseEntity<ApiResponse<ProfileDtos.ProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileDtos.UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật hồ sơ thành công",
                profileService.updateMyProfile(AuthUtils.currentUser(authentication), request)));
    }

    @GetMapping(ApiEndpoints.V1_STUDENT + "/resumes")
    public ResponseEntity<ApiResponse<List<ProfileDtos.ResumeResponse>>> listResumes(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách CV thành công",
                profileService.listResumes(AuthUtils.currentUser(authentication))));
    }

    @PostMapping(ApiEndpoints.V1_STUDENT + "/resumes")
    public ResponseEntity<ApiResponse<ProfileDtos.ResumeResponse>> createResume(
            Authentication authentication,
            @Valid @RequestBody ProfileDtos.ResumeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Thêm CV thành công",
                profileService.createResume(AuthUtils.currentUser(authentication), request)));
    }

    @PutMapping(ApiEndpoints.V1_STUDENT + "/resumes/{id}")
    public ResponseEntity<ApiResponse<ProfileDtos.ResumeResponse>> updateResume(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ProfileDtos.ResumeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật CV thành công",
                profileService.updateResume(AuthUtils.currentUser(authentication), id, request)));
    }

    @DeleteMapping(ApiEndpoints.V1_STUDENT + "/resumes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            Authentication authentication,
            @PathVariable Long id) {
        profileService.deleteResume(AuthUtils.currentUser(authentication), id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa CV", null));
    }

    @PatchMapping(ApiEndpoints.V1_STUDENT + "/resumes/{id}/default")
    public ResponseEntity<ApiResponse<ProfileDtos.ResumeResponse>> setDefaultResume(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đặt CV mặc định",
                profileService.setDefaultResume(AuthUtils.currentUser(authentication), id)));
    }
}
