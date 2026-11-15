package com.uniwork.service;

import com.uniwork.dto.ApplicationDtos;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.entity.*;
import com.uniwork.exception.AppException;
import com.uniwork.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final JobPostRepository jobPostRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudentResumeRepository studentResumeRepository;
    private final WorkAssignmentRepository workAssignmentRepository;
    private final ReviewRepository reviewRepository;
    private final ProvinceRepository provinceRepository;
    private final NotificationService notificationService;
    private final UserDisplayService userDisplayService;

    @Transactional
    public ApplicationDtos.ApplicationResponse apply(User user, Long jobId, ApplicationDtos.ApplyRequest request) {
        StudentProfile student = studentProfileRepository.findById(user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ sinh viên", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        JobPost job = jobPostRepository.findByIdAndDeletedAtIsNull(jobId)
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        if (!"OPEN".equals(job.getStatus())) {
            throw new AppException("Tin tuyển dụng không còn mở nhận hồ sơ", HttpStatus.CONFLICT, "JOB_NOT_OPEN");
        }
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new AppException("Tin tuyển dụng đã hết hạn nộp hồ sơ", HttpStatus.CONFLICT, "JOB_EXPIRED");
        }
        short filled = job.getPositionsFilled() == null ? 0 : job.getPositionsFilled();
        if (filled >= job.getPositionsCount()) {
            throw new AppException("Tin tuyển dụng đã đủ chỉ tiêu", HttpStatus.CONFLICT, "JOB_FULL");
        }
        if (applicationRepository.existsByJobPostIdAndStudentUserId(jobId, user.getId())) {
            throw new AppException("Bạn đã nộp đơn ứng tuyển cho công việc này rồi", HttpStatus.CONFLICT, "DUPLICATE_APPLICATION");
        }

        StudentResume resume = resolveResume(user, student, request);

        Application application = Application.builder()
                .jobPost(job)
                .student(student)
                .resume(resume)
                .coverLetter(request.coverLetter())
                .expectedSalary(request.expectedSalary())
                .status("PENDING")
                .build();
        application = applicationRepository.save(application);
        addHistory(application, null, "PENDING", user, "Sinh viên nộp đơn ứng tuyển");

        notificationService.create(
                job.getEmployer().getUser(),
                "APPLICATION_SUBMITTED",
                "Có đơn ứng tuyển mới",
                student.getFullName() + " đã ứng tuyển vào tin \"" + job.getTitle() + "\".",
                "APPLICATION",
                application.getId()
        );
        return toResponse(application, user, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationDtos.ApplicationResponse> listStudentApplications(
            User user, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> result = (status == null || status.isBlank())
                ? applicationRepository.findByStudentUserId(user.getId(), pageable)
                : applicationRepository.findByStudentUserIdAndStatus(user.getId(), status, pageable);
        return PageResponse.of(result.map(app -> toResponse(app, user, false)));
    }

    @Transactional(readOnly = true)
    public ApplicationDtos.ApplicationResponse getStudentApplication(User user, Long id) {
        Application application = applicationRepository.findByIdAndStudentUserId(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy đơn ứng tuyển", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        return toResponse(application, user, true);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse withdraw(User user, Long id) {
        Application application = applicationRepository.findByIdAndStudentUserId(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy đơn ứng tuyển", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (!"PENDING".equals(application.getStatus())) {
            throw new AppException("Chỉ có thể rút đơn đang chờ duyệt", HttpStatus.CONFLICT, "APPLICATION_NOT_PENDING");
        }
        String from = application.getStatus();
        application.setStatus("WITHDRAWN");
        application.setDecidedAt(LocalDateTime.now());
        applicationRepository.save(application);
        addHistory(application, from, "WITHDRAWN", user, "Sinh viên rút đơn ứng tuyển");
        notificationService.create(
                application.getJobPost().getEmployer().getUser(),
                "APPLICATION_WITHDRAWN",
                "Ứng viên đã rút đơn",
                application.getStudent().getFullName() + " đã rút đơn ứng tuyển tin \"" + application.getJobPost().getTitle() + "\".",
                "APPLICATION",
                application.getId()
        );
        return toResponse(application, user, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationDtos.ApplicationResponse> listJobApplications(
            User user, Long jobId, String status, int page, int size) {
        JobPost job = jobPostRepository.findByIdAndEmployerUserIdAndDeletedAtIsNull(jobId, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy tin tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "appliedAt"));
        Page<Application> result = (status == null || status.isBlank())
                ? applicationRepository.findByJobPostId(job.getId(), pageable)
                : applicationRepository.findByJobPostIdAndStatus(job.getId(), status, pageable);
        return PageResponse.of(result.map(app -> toResponse(app, user, false)));
    }

    @Transactional(readOnly = true)
    public ApplicationDtos.ApplicationResponse getEmployerApplication(User user, Long id) {
        Application application = applicationRepository.findByIdAndJobPostEmployerUserId(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy đơn ứng tuyển", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        return toResponse(application, user, true);
    }

    @Transactional
    public ApplicationDtos.ApplicationResponse updateStatus(
            User user, Long id, ApplicationDtos.StatusUpdateRequest request) {
        Application application = applicationRepository.findByIdAndJobPostEmployerUserId(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy đơn ứng tuyển", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        JobPost job = application.getJobPost();
        String target = "APPROVED".equalsIgnoreCase(request.status()) ? "ACCEPTED" : request.status().toUpperCase();
        String from = application.getStatus();

        if (List.of("ACCEPTED", "REJECTED", "WITHDRAWN").contains(from)) {
            throw new AppException("Đơn ứng tuyển đã được xử lý", HttpStatus.CONFLICT, "APPLICATION_NOT_PENDING");
        }
        if (!List.of("VIEWED", "SHORTLISTED", "ACCEPTED", "REJECTED").contains(target)) {
            throw new AppException("Trạng thái đơn ứng tuyển không hợp lệ", HttpStatus.BAD_REQUEST, "INVALID_APPLICATION_STATUS");
        }
        if (List.of("CLOSED", "CANCELLED", "COMPLETED", "IN_PROGRESS").contains(job.getStatus())) {
            throw new AppException("Tin tuyển dụng không còn nhận xét duyệt", HttpStatus.CONFLICT, "JOB_NOT_OPEN");
        }

        if ("ACCEPTED".equals(target)) {
            short filled = job.getPositionsFilled() == null ? 0 : job.getPositionsFilled();
            if (filled >= job.getPositionsCount()) {
                throw new AppException("Tin tuyển dụng đã đủ chỉ tiêu", HttpStatus.CONFLICT, "JOB_FULL");
            }
            application.setStatus("ACCEPTED");
            application.setDecidedAt(LocalDateTime.now());
            job.setPositionsFilled((short) (filled + 1));
            if (job.getPositionsFilled() >= job.getPositionsCount()) {
                job.setStatus("FILLED");
            }
            jobPostRepository.save(job);

            if (workAssignmentRepository.findByApplicationId(application.getId()).isEmpty()) {
                workAssignmentRepository.save(WorkAssignment.builder()
                        .application(application)
                        .jobPost(job)
                        .student(application.getStudent())
                        .employer(job.getEmployer())
                        .status("ONGOING")
                        .build());
            }
            notificationService.create(
                    application.getStudent().getUser(),
                    "APPLICATION_ACCEPTED",
                    "Đơn ứng tuyển được chấp nhận",
                    "Nhà tuyển dụng \"" + job.getEmployer().getDisplayName() + "\" đã chấp nhận đơn ứng tuyển tin \"" + job.getTitle() + "\".",
                    "APPLICATION",
                    application.getId()
            );
        } else if ("REJECTED".equals(target)) {
            application.setStatus("REJECTED");
            application.setRejectReason(request.reason());
            application.setDecidedAt(LocalDateTime.now());
            notificationService.create(
                    application.getStudent().getUser(),
                    "APPLICATION_REJECTED",
                    "Đơn ứng tuyển bị từ chối",
                    "Đơn ứng tuyển tin \"" + job.getTitle() + "\" đã bị từ chối."
                            + (request.reason() != null && !request.reason().isBlank() ? " Lý do: " + request.reason() : ""),
                    "APPLICATION",
                    application.getId()
            );
        } else {
            application.setStatus(target);
            application.setReviewedAt(LocalDateTime.now());
            if ("SHORTLISTED".equals(target)) {
                notificationService.create(
                        application.getStudent().getUser(),
                        "APPLICATION_SHORTLISTED",
                        "Đơn ứng tuyển được quan tâm",
                        "Nhà tuyển dụng đã đưa đơn ứng tuyển tin \"" + job.getTitle() + "\" vào danh sách quan tâm.",
                        "APPLICATION",
                        application.getId()
                );
            }
        }
        application = applicationRepository.save(application);
        addHistory(application, from, target, user, request.reason());
        return toResponse(application, user, true);
    }

    private StudentResume resolveResume(User user, StudentProfile student, ApplicationDtos.ApplyRequest request) {
        if (request.resumeId() != null) {
            return studentResumeRepository.findByIdAndStudentUserIdAndDeletedAtIsNull(request.resumeId(), user.getId())
                    .orElseThrow(() -> new AppException("Không tìm thấy CV đã chọn", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        }
        if (request.cvUrl() != null && !request.cvUrl().isBlank()) {
            return studentResumeRepository.findFirstByStudentUserIdAndFileUrlAndDeletedAtIsNull(user.getId(), request.cvUrl().trim())
                    .orElseGet(() -> studentResumeRepository.save(StudentResume.builder()
                            .student(student)
                            .title("CV ứng tuyển")
                            .fileUrl(request.cvUrl().trim())
                            .fileName(fileNameFromUrl(request.cvUrl()))
                            .isDefault(false)
                            .build()));
        }
        return studentResumeRepository.findFirstByStudentUserIdAndIsDefaultTrueAndDeletedAtIsNull(user.getId()).orElse(null);
    }

    private void addHistory(Application application, String from, String to, User actor, String note) {
        statusHistoryRepository.save(ApplicationStatusHistory.builder()
                .application(application)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(actor)
                .note(note)
                .build());
    }

    private ApplicationDtos.ApplicationResponse toResponse(Application application, User viewer, boolean detailed) {
        JobPost job = application.getJobPost();
        StudentProfile student = application.getStudent();
        User studentUser = student.getUser();
        EmployerProfile employer = job.getEmployer();
        StudentResume resume = application.getResume();
        WorkAssignment assignment = workAssignmentRepository.findByApplicationId(application.getId()).orElse(null);
        boolean reviewed = assignment != null
                && reviewRepository.existsByAssignmentIdAndReviewerId(assignment.getId(), viewer.getId());
        List<ApplicationDtos.HistoryItem> history = detailed
                ? statusHistoryRepository.findByApplicationIdOrderByChangedAtAsc(application.getId()).stream()
                .map(item -> new ApplicationDtos.HistoryItem(
                        item.getFromStatus(),
                        item.getToStatus(),
                        item.getNote(),
                        item.getChangedBy() == null ? "Hệ thống" : userDisplayService.displayName(item.getChangedBy()),
                        item.getChangedAt()))
                .toList()
                : List.of();

        return new ApplicationDtos.ApplicationResponse(
                application.getId(),
                job.getId(),
                job.getTitle(),
                job.getStatus(),
                job.getWorkMode(),
                job.getProvinceId(),
                job.getProvinceId() == null ? null
                        : provinceRepository.findById(job.getProvinceId()).map(Province::getName).orElse(null),
                job.getAddressDetail(),
                job.getSalaryType(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getStartDate(),
                job.getApplicationDeadline(),
                employer != null ? employer.getUserId() : null,
                employer != null ? employer.getDisplayName() : null,
                employer != null ? employer.getLogoUrl() : null,
                employer != null ? employer.getAvgRating() : null,
                employer != null ? employer.getRatingCount() : null,
                student.getUserId(),
                student.getFullName(),
                studentUser != null ? studentUser.getEmail() : null,
                student.getPhone(),
                student.getUniversity(),
                student.getMajor(),
                student.getYearOfStudy(),
                student.getBio(),
                student.getAvatarUrl(),
                application.getCoverLetter(),
                resume != null ? resume.getFileUrl() : null,
                resume != null ? resume.getTitle() : null,
                application.getExpectedSalary(),
                application.getStatus(),
                application.getEmployerNote(),
                application.getRejectReason(),
                application.getAppliedAt(),
                application.getReviewedAt(),
                application.getDecidedAt(),
                assignment != null ? assignment.getId() : null,
                assignment != null ? assignment.getStatus() : null,
                reviewed,
                history
        );
    }

    private String fileNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "cv";
        }
        String cleaned = url.split("\\?")[0];
        String[] parts = cleaned.split("/");
        return parts[parts.length - 1].isBlank() ? "cv" : parts[parts.length - 1];
    }

    private int clampSize(int size) {
        return size <= 0 ? 10 : Math.min(size, 50);
    }
}
