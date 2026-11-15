package com.uniwork.controller;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.dto.JobDtos;
import com.uniwork.dto.response.ApiResponse;
import com.uniwork.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API dữ liệu danh mục dùng chung: tỉnh thành, quận huyện, kỹ năng.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiEndpoints.V1_PUBLIC_MASTER)
public class MasterDataController {

    private final MasterDataService masterDataService;

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<JobDtos.ProvinceResponse>>> provinces() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách tỉnh thành thành công",
                masterDataService.listProvinces()));
    }

    @GetMapping("/provinces/{id}/districts")
    public ResponseEntity<ApiResponse<List<JobDtos.DistrictResponse>>> districts(@PathVariable Short id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách quận huyện thành công",
                masterDataService.listDistricts(id)));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<JobDtos.SkillResponse>>> skills() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách kỹ năng thành công",
                masterDataService.listSkills()));
    }
}
