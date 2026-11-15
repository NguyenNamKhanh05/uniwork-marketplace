package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.JobDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.service.MasterDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API công khai cho danh mục nghề nghiệp và dữ liệu danh mục.
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final MasterDataService masterDataService;

    @GetMapping(ApiEndpoints.V1_CATEGORIES)
    public ResponseEntity<ApiResponse<List<JobDtos.CategoryResponse>>> publicCategories() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh mục thành công",
                masterDataService.listActiveCategories()));
    }

    @GetMapping(ApiEndpoints.V1_ADMIN + "/categories")
    public ResponseEntity<ApiResponse<List<JobDtos.CategoryResponse>>> adminCategories() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách danh mục thành công",
                masterDataService.listAllCategories()));
    }

    @PostMapping(ApiEndpoints.V1_ADMIN + "/categories")
    public ResponseEntity<ApiResponse<JobDtos.CategoryResponse>> create(
            @Valid @RequestBody JobDtos.CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Thêm danh mục thành công",
                masterDataService.createCategory(request)));
    }

    @PutMapping(ApiEndpoints.V1_ADMIN + "/categories/{id}")
    public ResponseEntity<ApiResponse<JobDtos.CategoryResponse>> update(
            @PathVariable Short id,
            @Valid @RequestBody JobDtos.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật danh mục thành công",
                masterDataService.updateCategory(id, request)));
    }

    @PatchMapping(ApiEndpoints.V1_ADMIN + "/categories/{id}/visibility")
    public ResponseEntity<ApiResponse<JobDtos.CategoryResponse>> visibility(
            @PathVariable Short id,
            @Valid @RequestBody JobDtos.VisibilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Cập nhật trạng thái hiển thị thành công",
                masterDataService.setCategoryVisibility(id, request.visible())));
    }
}
