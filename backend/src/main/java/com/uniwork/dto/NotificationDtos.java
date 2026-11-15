package com.uniwork.dto;

import java.time.LocalDateTime;

public final class NotificationDtos {

    private NotificationDtos() {
    }

    public record NotificationResponse(
            Long id,
            String type,
            String title,
            String content,
            String refType,
            Long refId,
            Boolean read,
            LocalDateTime createdAt
    ) {
    }

    public record UnreadCountResponse(long unread) {
    }
}
