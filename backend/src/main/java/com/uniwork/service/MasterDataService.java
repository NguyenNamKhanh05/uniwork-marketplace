package com.uniwork.service;

import com.uniwork.dto.JobDtos;
import com.uniwork.entity.JobCategory;
import com.uniwork.entity.Province;
import com.uniwork.exception.AppException;
import com.uniwork.repository.DistrictRepository;
import com.uniwork.repository.JobCategoryRepository;
import com.uniwork.repository.ProvinceRepository;
import com.uniwork.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final JobCategoryRepository jobCategoryRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<JobDtos.CategoryResponse> listActiveCategories() {
        return jobCategoryRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toCategoryResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<JobDtos.CategoryResponse> listAllCategories() {
        return jobCategoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toCategoryResponse).toList();
    }

    @Transactional
    public JobDtos.CategoryResponse createCategory(JobDtos.CategoryRequest request) {
        String slug = request.slug() == null || request.slug().isBlank()
                ? slugify(request.name()) : slugify(request.slug());
        if (jobCategoryRepository.existsBySlug(slug)) {
            throw new AppException("Slug danh mục đã tồn tại", HttpStatus.CONFLICT, "CATEGORY_EXISTS");
        }
        JobCategory parent = null;
        if (request.parentId() != null) {
            parent = jobCategoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new AppException("Danh mục cha không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        }
        JobCategory category = JobCategory.builder()
                .name(request.name().trim())
                .slug(slug)
                .description(request.description())
                .sortOrder(request.sortOrder() == null ? (short) 0 : request.sortOrder())
                .isActive(request.isActive() == null || request.isActive())
                .parent(parent)
                .build();
        return toCategoryResponse(jobCategoryRepository.save(category));
    }

    @Transactional
    public JobDtos.CategoryResponse updateCategory(Short id, JobDtos.CategoryRequest request) {
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Danh mục không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        String slug = request.slug() == null || request.slug().isBlank()
                ? slugify(request.name()) : slugify(request.slug());
        if (!slug.equals(category.getSlug()) && jobCategoryRepository.existsBySlug(slug)) {
            throw new AppException("Slug danh mục đã tồn tại", HttpStatus.CONFLICT, "CATEGORY_EXISTS");
        }
        category.setName(request.name().trim());
        category.setSlug(slug);
        category.setDescription(request.description());
        if (request.sortOrder() != null) {
            category.setSortOrder(request.sortOrder());
        }
        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new AppException("Danh mục không thể là cha của chính nó", HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
            }
            category.setParent(jobCategoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new AppException("Danh mục cha không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND")));
        } else {
            category.setParent(null);
        }
        return toCategoryResponse(jobCategoryRepository.save(category));
    }

    @Transactional
    public JobDtos.CategoryResponse setCategoryVisibility(Short id, boolean visible) {
        JobCategory category = jobCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException("Danh mục không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        category.setIsActive(visible);
        return toCategoryResponse(jobCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<JobDtos.ProvinceResponse> listProvinces() {
        return provinceRepository.findAllByOrderByNameAsc().stream()
                .map(p -> new JobDtos.ProvinceResponse(p.getId(), p.getCode(), p.getName())).toList();
    }

    @Transactional(readOnly = true)
    public List<JobDtos.DistrictResponse> listDistricts(Short provinceId) {
        return districtRepository.findByProvinceIdOrderByNameAsc(provinceId).stream()
                .map(d -> new JobDtos.DistrictResponse(d.getId(), d.getProvince().getId(), d.getCode(), d.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobDtos.SkillResponse> listSkills() {
        return skillRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(s -> new JobDtos.SkillResponse(s.getId(), s.getName(), s.getSlug())).toList();
    }

    private JobDtos.CategoryResponse toCategoryResponse(JobCategory category) {
        return new JobDtos.CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder(),
                category.getIsActive(),
                category.getParent() != null ? category.getParent().getId() : null
        );
    }

    public static String slugify(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d").replace("Đ", "D");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
