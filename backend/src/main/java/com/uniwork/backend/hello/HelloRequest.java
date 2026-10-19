package com.uniwork.backend.hello;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HelloRequest(
        @NotBlank(message = "Nội dung không được để trống")
        @Size(max = 255, message = "Nội dung tối đa 255 ký tự")
        String content
) {
}
