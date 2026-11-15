package com.uniwork.service;

import com.uniwork.dto.NotificationDtos;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.entity.Notification;
import com.uniwork.entity.User;
import com.uniwork.exception.AppException;
import com.uniwork.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void create(User user, String type, String title, String content, String refType, Long refId) {
        if (user == null) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .refType(refType)
                .refId(refId)
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDtos.NotificationResponse> list(Long userId, boolean unreadOnly, Pageable pageable) {
        var page = unreadOnly
                ? notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationDtos.NotificationResponse markRead(Long userId, Long id) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException("Không tìm thấy thông báo", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId, LocalDateTime.now());
    }

    private NotificationDtos.NotificationResponse toResponse(Notification notification) {
        return new NotificationDtos.NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRefType(),
                notification.getRefId(),
                Boolean.TRUE.equals(notification.getIsRead()),
                notification.getCreatedAt()
        );
    }
}
