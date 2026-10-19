package com.uniwork.backend.hello;

import java.time.Instant;

public record HelloMessageResponse(Long id, String content, Instant createdAt) {

    public static HelloMessageResponse from(HelloMessage message) {
        return new HelloMessageResponse(message.getId(), message.getContent(), message.getCreatedAt());
    }
}
