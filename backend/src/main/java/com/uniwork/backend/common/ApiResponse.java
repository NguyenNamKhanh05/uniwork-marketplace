package com.uniwork.backend.common;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        List<FieldErrorDetail> errors,
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "Thành công", data, List.of(), Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data, List.of(), Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, List<FieldErrorDetail> errors) {
        return new ApiResponse<>(false, code, message, null, errors, Instant.now());
    }
}
