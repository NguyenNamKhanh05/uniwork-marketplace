package com.uniwork.service;

import com.uniwork.dto.JobDtos;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.entity.*;
import com.uniwork.exception.AppException;
import com.uniwork.repository.*;
import com.uniwork.repository.specification.JobPostSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobPostRepository jobPostRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final SkillRepository skillRepository;
    private final JobPostSkillRepository jobPostSkillRepository;
    private final JobPostScheduleRepository jobPostScheduleRepository;
    private final ApplicationRepository applicationRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final SavedJobRepository savedJobRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationService notificationService;
    private final UserDisplayService userDisplayService;

    @Transactional(readOnly = true)
    public PageResponse<JobDtos.JobResponse> search(
            String keyword, Short categoryId, Short provinceId, String workMode,
            String salaryType, BigDecimal minSalary, BigDecimal maxSalary,
            int page, int size, String sort
    ) {
        Pageable pageable = pageable(page, size, sort);
        Page<JobPost> result = jobPostRepository.findAll(
                JobPostSpecification.filterPublicJobs(keyword, categoryId, provinceId, workMode,
                        salaryType, minSalary, maxSalary),
                pageable);
        return PageResponse.of(result.map(job -> toResponse(job, false)));
    }

    @Transactional
    public JobDtos.JobResponse getPublicDetail(Long id) {
        JobPost job = jobPostRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (!"OPEN".equals(job.getStatus())) {
            throw new AppException("Tin tuyển dụng không còn mở", HttpStatus.NOT_FOUND, "JOB_NOT_OPEN");
        }
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new AppException("Tin tuyển dụng đã hết hạn nộp hồ sơ", HttpStatus.NOT_FOUND, "JOB_EXPIRED");
        }
        job.setViewCount(job.getViewCount() == null ? 1 : job.getViewCount() + 1);
        jobPostRepository.save(job);
        return toResponse(job, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobDtos.JobResponse> listEmployerJobs(User user, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobPost> result = (status == null || status.isBlank())
                ? jobPostRepository.findByEmployerUserIdAndDeletedAtIsNull(user.getId(), pageable)
                : jobPostRepository.findByEmployerUserIdAndStatusAndDeletedAtIsNull(user.getId(), status, pageable);
        return PageResponse.of(result.map(job -> toResponse(job, false)));
    }

    @Transactional(readOnly = true)
    public JobDtos.JobResponse getEmployerJob(User user, Long id) {
        JobPost job = jobPostRepository.findByIdAndEmployerUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        return toResponse(job, true);
    }

    @Transactional
    public JobDtos.JobResponse createJob(User user, JobDtos.JobRequest request) {
        EmployerProfile employer = employerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ nhà tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        JobCategory category = jobCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException("Danh mục không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        validateJobRequest(request);

        JobPost job = JobPost.builder()
                .employer(employer)
                .category(category)
                .title(request.title().trim())
                .description(request.description().trim())
                .requirements(request.requirements())
                .benefits(request.benefits())
                .workMode(request.workMode())
                .provinceId(request.provinceId())
                .districtId(request.districtId())
                .addressDetail(request.addressDetail())
                .salaryType(normalizeSalaryType(request.salaryType()))
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .positionsCount(request.positionsCount())
                .positionsFilled((short) 0)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .applicationDeadline(request.applicationDeadline())
                .status("OPEN")
                .publishedAt(LocalDateTime.now())
                .viewCount(0)
                .build();

        job = jobPostRepository.save(job);
        saveSkillsAndSchedules(job, request.skillIds(), request.schedules());
        return toResponse(job, true);
    }

    @Transactional
    public JobDtos.JobResponse updateJob(User user, Long id, JobDtos.JobRequest request) {
        JobPost job = jobPostRepository.findByIdAndEmployerUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        JobCategory category = jobCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException("Danh mục không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        validateJobRequest(request);

        job.setCategory(category);
        job.setTitle(request.title().trim());
        job.setDescription(request.description().trim());
        job.setRequirements(request.requirements());
        job.setBenefits(request.benefits());
        job.setWorkMode(request.workMode());
        job.setProvinceId(request.provinceId());
        job.setDistrictId(request.districtId());
        job.setAddressDetail(request.addressDetail());
        job.setSalaryType(normalizeSalaryType(request.salaryType()));
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        if (request.positionsCount() != null && request.positionsCount() >= (job.getPositionsFilled() == null ? 0 : job.getPositionsFilled())) {
            job.setPositionsCount(request.positionsCount());
        }
        job.setStartDate(request.startDate());
        job.setEndDate(request.endDate());
        job.setApplicationDeadline(request.applicationDeadline());
        job = jobPostRepository.save(job);

        jobPostSkillRepository.deleteByJobPostId(job.getId());
        jobPostSkillRepository.flush();
        jobPostScheduleRepository.deleteByJobPostId(job.getId());
        jobPostScheduleRepository.flush();
        saveSkillsAndSchedules(job, request.skillIds(), request.schedules());
        return toResponse(job, true);
    }

    @Transactional
    public JobDtos.JobResponse closeJob(User user, Long id) {
        JobPost job = jobPostRepository.findByIdAndEmployerUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if ("COMPLETED".equals(job.getStatus()) || "CANCELLED".equals(job.getStatus())) {
            throw new AppException("Không thể đóng tin đã hoàn thành hoặc đã hủy", HttpStatus.CONFLICT, "INVALID_JOB_STATUS");
        }
        job.setStatus("CLOSED");
        job.setClosedAt(LocalDateTime.now());
        return toResponse(jobPostRepository.save(job), true);
    }

    @Transactional
    public JobDtos.JobResponse updateJobStatus(User user, Long id, String targetStatus) {
        JobPost job = jobPostRepository.findByIdAndEmployerUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        if ("IN_PROGRESS".equals(targetStatus)) {
            if (!"OPEN".equals(job.getStatus()) && !"FILLED".equals(job.getStatus())) {
                throw new AppException("Chỉ có thể bắt đầu từ tin đang tuyển hoặc đã đủ người", HttpStatus.CONFLICT, "INVALID_JOB_STATUS");
            }
            job.setStatus("IN_PROGRESS");
            for (WorkAssignment assignment : workAssignmentRepository.findByJobPostId(job.getId())) {
                if (assignment.getStartedAt() == null) {
                    assignment.setStartedAt(LocalDateTime.now());
                    workAssignmentRepository.save(assignment);
                }
            }
        } else if ("COMPLETED".equals(targetStatus)) {
            if (!"IN_PROGRESS".equals(job.getStatus())) {
                throw new AppException("Chỉ có thể hoàn thành khi công việc đang thực hiện", HttpStatus.CONFLICT, "INVALID_JOB_STATUS");
            }
            job.setStatus("COMPLETED");
            job.setClosedAt(LocalDateTime.now());
            for (WorkAssignment assignment : workAssignmentRepository.findByJobPostId(job.getId())) {
                assignment.setStatus("COMPLETED");
                assignment.setEndedAt(LocalDateTime.now());
                assignment.setEmployerConfirmedAt(LocalDateTime.now());
                workAssignmentRepository.save(assignment);
                notificationService.create(
                        assignment.getStudent().getUser(),
                        "REVIEW_REQUEST",
                        "Công việc đã hoàn thành",
                        "Công việc \"" + job.getTitle() + "\" đã hoàn thành. Bạn có thể gửi đánh giá cho nhà tuyển dụng.",
                        "APPLICATION",
                        assignment.getApplication().getId()
                );
            }
        } else if ("CLOSED".equals(targetStatus)) {
            job.setStatus("CLOSED");
            job.setClosedAt(LocalDateTime.now());
        } else {
            throw new AppException("Trạng thái công việc không hợp lệ", HttpStatus.BAD_REQUEST, "INVALID_JOB_STATUS");
        }
        return toResponse(jobPostRepository.save(job), true);
    }

    @Transactional
    public void setVisibility(User admin, Long id, boolean hidden, String reason) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (hidden && (reason == null || reason.isBlank())) {
            throw new AppException("Vui lòng nhập lý do ẩn tin", HttpStatus.BAD_REQUEST, "REASON_REQUIRED");
        }
        job.setDeletedAt(hidden ? LocalDateTime.now() : null);
        if (hidden) {
            job.setRejectReason(reason);
        }
        jobPostRepository.save(job);
        notificationService.create(
                job.getEmployer().getUser(),
                hidden ? "JOB_HIDDEN" : "JOB_VISIBLE",
                hidden ? "Tin tuyển dụng đã bị ẩn" : "Tin tuyển dụng đã được hiển thị lại",
                hidden
                        ? "Tin \"" + job.getTitle() + "\" đã bị quản trị viên ẩn. Lý do: " + reason
                        : "Tin \"" + job.getTitle() + "\" đã được quản trị viên hiển thị lại.",
                "JOB_POST",
                job.getId()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<com.uniwork.dto.AdminDtos.AdminJobResponse> adminJobs(
            String keyword, String status, Boolean hidden, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobPost> result = jobPostRepository.findAll(
                JobPostSpecification.filterAdminJobs(keyword, status, hidden), pageable);
        return PageResponse.of(result.map(job -> new com.uniwork.dto.AdminDtos.AdminJobResponse(
                job.getId(),
                job.getTitle(),
                job.getEmployer() != null ? job.getEmployer().getDisplayName() : null,
                job.getCategory() != null ? job.getCategory().getName() : null,
                job.getStatus(),
                job.getDeletedAt() != null,
                applicationRepository.countByJobPostId(job.getId()),
                job.getPositionsCount(),
                job.getPositionsFilled(),
                job.getCreatedAt())));
    }

    @Transactional(readOnly = true)
    public JobPost findJobEntity(Long id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    @Transactional
    public void saveJob(User user, Long jobId) {
        if (savedJobRepository.existsByStudentIdAndJobPostId(user.getId(), jobId)) {
            return;
        }
        if (studentProfileRepository.findById(user.getId()).isEmpty()) {
            throw new AppException("Không tìm thấy hồ sơ sinh viên", HttpStatus.NOT_FOUND, "NOT_FOUND");
        }
        JobPost job = jobPostRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        savedJobRepository.save(SavedJob.builder()
                .studentId(user.getId())
                .jobPostId(job.getId())
                .build());
    }

    @Transactional
    public void unsaveJob(User user, Long jobId) {
        savedJobRepository.deleteByStudentIdAndJobPostId(user.getId(), jobId);
    }

    @Transactional(readOnly = true)
    public PageResponse<JobDtos.JobResponse> listSavedJobs(User user, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SavedJob> result = savedJobRepository.findByStudentId(user.getId(), pageable);
        return PageResponse.of(result.map(item -> {
            JobDtos.JobResponse response = toResponse(item.getJobPost(), false);
            return new JobDtos.JobResponse(
                    response.id(), response.title(), response.categoryId(), response.categoryName(),
                    response.description(), response.requirements(), response.benefits(), response.workMode(),
                    response.provinceId(), response.provinceName(), response.districtId(), response.districtName(),
                    response.addressDetail(), response.salaryType(), response.salaryMin(), response.salaryMax(),
                    response.salaryCurrency(), response.positionsCount(), response.positionsFilled(),
                    response.startDate(), response.endDate(), response.applicationDeadline(), response.status(),
                    response.viewCount(), response.publishedAt(), response.createdAt(), response.employerId(),
                    response.employerName(), response.employerVerified(), response.employerRating(),
                    response.employerRatingCount(), response.employerLogoUrl(), response.employerDescription(),
                    response.applicationCount(), true, response.skills(), response.schedules());
        }));
    }

    private void validateJobRequest(JobDtos.JobRequest request) {
        String salaryType = normalizeSalaryType(request.salaryType());
        if (!"NEGOTIABLE".equals(salaryType) && request.salaryMin() == null) {
            throw new AppException("Vui lòng nhập mức lương tối thiểu", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        }
        if (!"NEGOTIABLE".equals(salaryType) && request.salaryMin() != null
                && request.salaryMin().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Mức lương phải lớn hơn 0", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        }
        if (request.salaryMin() != null && request.salaryMax() != null
                && request.salaryMax().compareTo(request.salaryMin()) < 0) {
            throw new AppException("Mức lương tối đa phải lớn hơn hoặc bằng mức lương tối thiểu", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        }
        if (request.applicationDeadline() != null
                && request.applicationDeadline().toLocalDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new AppException("Hạn nộp phải sau ngày hôm nay ít nhất 1 ngày", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        }
        if (request.startDate() != null && request.endDate() != null
                && request.endDate().isBefore(request.startDate())) {
            throw new AppException("Ngày kết thúc phải sau ngày bắt đầu", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        }
        if (request.schedules() != null) {
            for (JobDtos.ScheduleRequest schedule : request.schedules()) {
                if (!schedule.endTime().isAfter(schedule.startTime())) {
                    throw new AppException("Giờ kết thúc ca phải sau giờ bắt đầu", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
                }
            }
        }
    }

    private void saveSkillsAndSchedules(JobPost job, List<Integer> skillIds, List<JobDtos.ScheduleRequest> schedules) {
        if (skillIds != null) {
            List<JobPostSkill> items = new ArrayList<>();
            for (Integer skillId : skillIds) {
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new AppException("Kỹ năng không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
                items.add(JobPostSkill.builder()
                        .jobPostId(job.getId())
                        .skillId(skill.getId())
                        .skill(skill)
                        .build());
            }
            jobPostSkillRepository.saveAll(items);
        }
        if (schedules != null) {
            List<JobPostSchedule> items = new ArrayList<>();
            for (JobDtos.ScheduleRequest schedule : schedules) {
                items.add(JobPostSchedule.builder()
                        .jobPost(job)
                        .dayOfWeek(schedule.dayOfWeek())
                        .startTime(schedule.startTime())
                        .endTime(schedule.endTime())
                        .build());
            }
            jobPostScheduleRepository.saveAll(items);
        }
    }

    private JobDtos.JobResponse toResponse(JobPost job, boolean detailed) {
        EmployerProfile employer = job.getEmployer();
        String provinceName = job.getProvinceId() == null ? null
                : provinceRepository.findById(job.getProvinceId()).map(Province::getName).orElse(null);
        String districtName = job.getDistrictId() == null ? null
                : districtRepository.findById(job.getDistrictId()).map(District::getName).orElse(null);
        List<JobDtos.SkillItem> skills = new ArrayList<>();
        List<JobDtos.ScheduleItem> schedules = new ArrayList<>();
        if (detailed) {
            for (JobPostSkill item : jobPostSkillRepository.findByJobPostId(job.getId())) {
                Skill skill = item.getSkill();
                skills.add(new JobDtos.SkillItem(
                        skill != null ? skill.getId().longValue() : item.getSkillId().longValue(),
                        skill != null ? skill.getName() : "",
                        null));
            }
            for (JobPostSchedule item : jobPostScheduleRepository
                    .findByJobPostIdOrderByDayOfWeekAscStartTimeAsc(job.getId())) {
                schedules.add(new JobDtos.ScheduleItem(item.getDayOfWeek(), item.getStartTime(), item.getEndTime()));
            }
        }
        return new JobDtos.JobResponse(
                job.getId(),
                job.getTitle(),
                job.getCategory() != null ? job.getCategory().getId() : null,
                job.getCategory() != null ? job.getCategory().getName() : null,
                detailed ? job.getDescription() : null,
                detailed ? job.getRequirements() : null,
                detailed ? job.getBenefits() : null,
                job.getWorkMode(),
                job.getProvinceId(),
                provinceName,
                job.getDistrictId(),
                districtName,
                job.getAddressDetail(),
                job.getSalaryType(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.getPositionsCount(),
                job.getPositionsFilled(),
                job.getStartDate(),
                job.getEndDate(),
                job.getApplicationDeadline(),
                job.getStatus(),
                job.getViewCount(),
                job.getPublishedAt(),
                job.getCreatedAt(),
                employer != null ? employer.getUserId() : null,
                employer != null ? employer.getDisplayName() : null,
                employer != null ? employer.getIsVerified() : null,
                employer != null ? employer.getAvgRating() : null,
                employer != null ? employer.getRatingCount() : null,
                employer != null ? employer.getLogoUrl() : null,
                employer != null ? employer.getDescription() : null,
                applicationRepository.countByJobPostId(job.getId()),
                null,
                skills,
                schedules
        );
    }

    private Pageable pageable(int page, int size, String sort) {
        Sort sorting = Sort.by(Sort.Direction.DESC, "publishedAt");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0];
            if (List.of("createdAt", "publishedAt", "salaryMin", "salaryMax", "title", "applicationDeadline").contains(field)) {
                Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                sorting = Sort.by(direction, field);
            }
        }
        return PageRequest.of(Math.max(page, 0), clampSize(size), sorting);
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 50);
    }

    private String normalizeSalaryType(String salaryType) {
        if ("PER_SHIFT".equals(salaryType)) {
            return "DAILY";
        }
        if ("PER_JOB".equals(salaryType)) {
            return "PER_TASK";
        }
        return salaryType;
    }
}
