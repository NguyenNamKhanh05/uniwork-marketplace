package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.ReviewDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API đánh giá hai chiều sau khi công việc hoàn thành.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.V1_REVIEWS)
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDtos.ReviewResponse>> create(
            Authentication authentication,
            @Valid @RequestBody ReviewDtos.ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Gửi đánh giá thành công",
                reviewService.create(AuthUtils.currentUser(authentication), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewDtos.ReviewResponse>>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đánh giá thành công",
                reviewService.list(userId, jobId, page, size)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ReviewDtos.PendingReviewResponse>>> pending(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đánh giá chờ xử lý thành công",
                reviewService.pending(AuthUtils.currentUser(authentication))));
    }
}
