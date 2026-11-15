package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.NotificationDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.security.AuthUtils;
import com.uniwork.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API thông báo cho người dùng đã đăng nhập.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.V1_NOTIFICATIONS)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDtos.NotificationResponse>>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = size <= 0 ? 10 : Math.min(size, 50);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách thông báo thành công",
                notificationService.list(
                        AuthUtils.currentUser(authentication).getId(),
                        unreadOnly,
                        PageRequest.of(Math.max(page, 0), safeSize))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationDtos.UnreadCountResponse>> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy số thông báo chưa đọc thành công",
                new NotificationDtos.UnreadCountResponse(
                        notificationService.unreadCount(AuthUtils.currentUser(authentication).getId()))));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDtos.NotificationResponse>> markRead(
            Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đánh dấu thông báo là đã đọc",
                notificationService.markRead(AuthUtils.currentUser(authentication).getId(), id)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllRead(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã đánh dấu tất cả thông báo là đã đọc",
                notificationService.markAllRead(AuthUtils.currentUser(authentication).getId())));
    }
}
