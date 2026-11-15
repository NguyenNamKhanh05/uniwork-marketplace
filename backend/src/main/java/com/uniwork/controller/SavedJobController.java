package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.JobDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API việc làm đã lưu của sinh viên.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.V1_STUDENT + "/saved-jobs")
public class SavedJobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobDtos.JobResponse>>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách việc làm đã lưu thành công",
                jobService.listSavedJobs(AuthUtils.currentUser(authentication), page, size)));
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> save(Authentication authentication, @PathVariable Long jobId) {
        jobService.saveJob(AuthUtils.currentUser(authentication), jobId);
        return ResponseEntity.ok(ApiResponse.success("Đã lưu việc làm", null));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> unsave(Authentication authentication, @PathVariable Long jobId) {
        jobService.unsaveJob(AuthUtils.currentUser(authentication), jobId);
        return ResponseEntity.ok(ApiResponse.success("Đã bỏ lưu việc làm", null));
    }
}
