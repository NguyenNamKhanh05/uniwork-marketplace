package com.uniwork.service;

import com.uniwork.dto.AdminDtos;
import com.uniwork.dto.response.PageResponse;
import com.uniwork.entity.AuditLog;
import com.uniwork.entity.User;
import com.uniwork.exception.AppException;
import com.uniwork.repository.ApplicationRepository;
import com.uniwork.repository.AuditLogRepository;
import com.uniwork.repository.JobPostRepository;
import com.uniwork.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final UserDisplayService userDisplayService;

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.UserAdminResponse> listUsers(
            String keyword, String role, String status, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("email")), pattern));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role").get("code"), role.trim().toUpperCase()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> result = userRepository.findAll(specification, pageable);
        return PageResponse.of(result.map(this::toUserResponse));
    }

    @Transactional
    public AdminDtos.UserAdminResponse updateUserStatus(
            User admin, Long userId, AdminDtos.UserStatusRequest request, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        String target = request.status() == null ? "" : request.status().trim().toUpperCase();
        if (!List.of("ACTIVE", "LOCKED", "DELETED").contains(target)) {
            throw new AppException("Trạng thái tài khoản không hợp lệ", HttpStatus.BAD_REQUEST, "INVALID_USER_STATUS");
        }
        if (user.getId().equals(admin.getId())) {
            throw new AppException("Không thể thay đổi trạng thái tài khoản của chính mình", HttpStatus.BAD_REQUEST, "INVALID_USER_STATUS");
        }
        user.setStatus(target);
        if ("ACTIVE".equals(target)) {
            user.setLockedUntil(null);
        }
        if ("DELETED".equals(target)) {
            user.setDeletedAt(LocalDateTime.now());
        } else {
            user.setDeletedAt(null);
        }
        userRepository.save(user);

        auditService.log(admin, "UPDATE_USER_STATUS", "USER", user.getId(), ipAddress);
        String title = switch (target) {
            case "LOCKED" -> "Tài khoản đã bị khóa";
            case "ACTIVE" -> "Tài khoản đã được mở khóa";
            default -> "Tài khoản đã bị xóa";
        };
        notificationService.create(
                user,
                "ACCOUNT_" + target,
                title,
                request.reason() == null || request.reason().isBlank()
                        ? title + " bởi quản trị viên."
                        : title + ". Lý do: " + request.reason(),
                "USER",
                user.getId());
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public AdminDtos.DashboardResponse dashboard() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.countByRoleCode("STUDENT");
        long totalEmployers = userRepository.countByRoleCode("EMPLOYER");
        long totalJobs = jobPostRepository.count();
        long openJobs = jobPostRepository.countByStatusAndDeletedAtIsNull("OPEN");
        long totalApplications = applicationRepository.count();
        long pendingApplications = applicationRepository.countByStatus("PENDING");
        return new AdminDtos.DashboardResponse(
                totalUsers, totalStudents, totalEmployers, totalJobs, openJobs,
                totalApplications, pendingApplications);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminDtos.AuditLogResponse> auditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size));
        Page<AuditLog> result = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.of(result.map(log -> new AdminDtos.AuditLogResponse(
                log.getId(),
                log.getActor() == null ? "Hệ thống" : userDisplayService.displayName(log.getActor()),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getIpAddress(),
                log.getCreatedAt())));
    }

    private AdminDtos.UserAdminResponse toUserResponse(User user) {
        return new AdminDtos.UserAdminResponse(
                user.getId(),
                user.getEmail(),
                userDisplayService.displayName(user),
                user.getRole() != null ? user.getRole().getCode() : null,
                user.getStatus(),
                user.getAuthProvider(),
                user.getCreatedAt(),
                user.getLastLoginAt());
    }

    private int clampSize(int size) {
        return size <= 0 ? 10 : Math.min(size, 50);
    }
}
