package com.uniwork.backend.hello;

import com.uniwork.backend.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final HelloMessageRepository repository;

    public HelloController(HelloMessageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.success(Map.of(
                "message", "UniWork backend is running",
                "database", "MySQL",
                "savedMessages", repository.count()
        ));
    }

    @GetMapping
    public ApiResponse<List<HelloMessageResponse>> getAll() {
        List<HelloMessageResponse> messages = repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(HelloMessageResponse::from)
                .toList();
        return ApiResponse.success(messages);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelloMessageResponse>> create(@Valid @RequestBody HelloRequest request) {
        HelloMessage saved = repository.save(new HelloMessage(request.content().trim()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã lưu dữ liệu vào MySQL", HelloMessageResponse.from(saved)));
    }
}
