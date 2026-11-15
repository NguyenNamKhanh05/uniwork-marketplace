package com.uniwork.service;

import com.uniwork.dto.JobDtos;
import com.uniwork.dto.ProfileDtos;
import com.uniwork.entity.*;
import com.uniwork.exception.AppException;
import com.uniwork.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentResumeRepository studentResumeRepository;
    private final SkillRepository skillRepository;
    private final ProvinceRepository provinceRepository;

    @Transactional(readOnly = true)
    public ProfileDtos.ProfileResponse getMyProfile(User user) {
        User current = userRepository.findById(user.getId())
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        String role = current.getRole().getCode();
        String provinceName = null;
        List<JobDtos.SkillItem> skills = new ArrayList<>();
        List<ProfileDtos.ResumeResponse> resumes = new ArrayList<>();

        ProfileDtos.ProfileResponse response = new ProfileDtos.ProfileResponse(
                current.getId(), current.getEmail(), role,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                skills, resumes
        );

        if ("STUDENT".equals(role)) {
            StudentProfile profile = studentProfileRepository.findById(current.getId())
                    .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ sinh viên", HttpStatus.NOT_FOUND, "NOT_FOUND"));
            provinceName = provinceName(profile.getProvinceId());
            for (StudentSkill studentSkill : studentSkillRepository.findByStudentId(current.getId())) {
                Skill skill = studentSkill.getSkill();
                skills.add(new JobDtos.SkillItem(
                        skill != null ? skill.getId().longValue() : studentSkill.getSkillId().longValue(),
                        skill != null ? skill.getName() : "",
                        studentSkill.getLevel()));
            }
            for (StudentResume resume : studentResumeRepository
                    .findByStudentUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(current.getId())) {
                resumes.add(toResumeResponse(resume));
            }
            return new ProfileDtos.ProfileResponse(
                    current.getId(), current.getEmail(), role,
                    profile.getFullName(), profile.getPhone(), profile.getDateOfBirth(), profile.getGender(),
                    profile.getStudentCode(), profile.getUniversity(), profile.getMajor(), profile.getYearOfStudy(),
                    profile.getProvinceId(), provinceName, profile.getAddress(), profile.getBio(), profile.getAvatarUrl(),
                    profile.getAvgRating(), profile.getRatingCount(),
                    null, null, null, null, null, null, null, null, null, null,
                    skills, resumes
            );
        }

        if ("EMPLOYER".equals(role)) {
            EmployerProfile profile = employerProfileRepository.findById(current.getId())
                    .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ nhà tuyển dụng", HttpStatus.NOT_FOUND, "NOT_FOUND"));
            provinceName = provinceName(profile.getProvinceId());
            return new ProfileDtos.ProfileResponse(
                    current.getId(), current.getEmail(), role,
                    null, null, null, null, null, null, null, null,
                    profile.getProvinceId(), provinceName, profile.getAddress(), profile.getDescription(), profile.getLogoUrl(),
                    profile.getAvgRating(), profile.getRatingCount(),
                    profile.getEmployerType(), profile.getDisplayName(), profile.getContactName(), profile.getContactPhone(),
                    profile.getTaxCode(), profile.getDescription(), profile.getLogoUrl(), profile.getWebsite(),
                    profile.getIsVerified(), profile.getVerifiedAt(),
                    skills, resumes
            );
        }

        return response;
    }

    @Transactional
    public ProfileDtos.ProfileResponse updateMyProfile(User user, ProfileDtos.UpdateProfileRequest request) {
        User current = userRepository.findById(user.getId())
                .orElseThrow(() -> new AppException("Người dùng không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        String role = current.getRole().getCode();

        if ("STUDENT".equals(role)) {
            StudentProfile profile = studentProfileRepository.findById(current.getId())
                    .orElseGet(() -> StudentProfile.builder().user(current).fullName(current.getEmail()).build());
            if (request.fullName() != null) {
                if (request.fullName().isBlank()) {
                    throw new AppException("Họ tên không được để trống", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
                }
                profile.setFullName(request.fullName().trim());
            }
            profile.setPhone(clean(request.phone()));
            profile.setDateOfBirth(request.dateOfBirth());
            profile.setGender(clean(request.gender()));
            profile.setStudentCode(clean(request.studentCode()));
            profile.setUniversity(clean(request.university()));
            profile.setMajor(clean(request.major()));
            profile.setYearOfStudy(request.yearOfStudy());
            profile.setProvinceId(request.provinceId());
            profile.setAddress(clean(request.address()));
            profile.setBio(clean(request.bio()));
            profile.setAvatarUrl(clean(request.avatarUrl()));
            studentProfileRepository.save(profile);

            if (request.skills() != null) {
                studentSkillRepository.deleteByStudentId(current.getId());
                studentSkillRepository.flush();
                List<StudentSkill> skills = new ArrayList<>();
                for (ProfileDtos.StudentSkillRequest item : request.skills()) {
                    Skill skill = skillRepository.findById(item.skillId())
                            .orElseThrow(() -> new AppException("Kỹ năng không tồn tại", HttpStatus.NOT_FOUND, "NOT_FOUND"));
                    String level = item.level() == null || item.level().isBlank() ? "BEGINNER" : item.level();
                    skills.add(StudentSkill.builder()
                            .studentId(current.getId())
                            .skillId(skill.getId())
                            .skill(skill)
                            .level(level)
                            .build());
                }
                studentSkillRepository.saveAll(skills);
            }
            return getMyProfile(current);
        }

        if ("EMPLOYER".equals(role)) {
            EmployerProfile profile = employerProfileRepository.findById(current.getId())
                    .orElseGet(() -> EmployerProfile.builder()
                            .user(current)
                            .displayName(current.getEmail())
                            .employerType("COMPANY")
                            .build());
            if (request.displayName() != null) {
                if (request.displayName().isBlank()) {
                    throw new AppException("Tên đơn vị không được để trống", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
                }
                profile.setDisplayName(request.displayName().trim());
            }
            if (request.employerType() != null && !request.employerType().isBlank()) {
                profile.setEmployerType(request.employerType());
            }
            profile.setContactName(clean(request.contactName()));
            profile.setContactPhone(clean(request.contactPhone()));
            profile.setTaxCode(clean(request.taxCode()));
            profile.setDescription(clean(request.description()));
            profile.setLogoUrl(clean(request.logoUrl()));
            profile.setWebsite(clean(request.website()));
            profile.setProvinceId(request.provinceId());
            profile.setAddress(clean(request.address()));
            employerProfileRepository.save(profile);
            return getMyProfile(current);
        }

        throw new AppException("Vai trò này không có hồ sơ cá nhân", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    @Transactional(readOnly = true)
    public List<ProfileDtos.ResumeResponse> listResumes(User user) {
        requireStudent(user);
        return studentResumeRepository.findByStudentUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResumeResponse).toList();
    }

    @Transactional
    public ProfileDtos.ResumeResponse createResume(User user, ProfileDtos.ResumeRequest request) {
        StudentProfile student = requireStudent(user);
        boolean isDefault = Boolean.TRUE.equals(request.isDefault())
                || studentResumeRepository.findByStudentUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(user.getId()).isEmpty();
        if (isDefault) {
            clearDefaultResumes(user.getId());
        }
        StudentResume resume = StudentResume.builder()
                .student(student)
                .title(request.title().trim())
                .fileUrl(request.fileUrl().trim())
                .fileName(request.fileName() == null || request.fileName().isBlank()
                        ? defaultFileName(request.fileUrl()) : request.fileName().trim())
                .isDefault(isDefault)
                .build();
        return toResumeResponse(studentResumeRepository.save(resume));
    }

    @Transactional
    public ProfileDtos.ResumeResponse updateResume(User user, Long id, ProfileDtos.ResumeRequest request) {
        requireStudent(user);
        StudentResume resume = studentResumeRepository.findByIdAndStudentUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy CV", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultResumes(user.getId());
            resume.setIsDefault(true);
        }
        resume.setTitle(request.title().trim());
        resume.setFileUrl(request.fileUrl().trim());
        resume.setFileName(request.fileName() == null || request.fileName().isBlank()
                ? defaultFileName(request.fileUrl()) : request.fileName().trim());
        return toResumeResponse(studentResumeRepository.save(resume));
    }

    @Transactional
    public void deleteResume(User user, Long id) {
        requireStudent(user);
        StudentResume resume = studentResumeRepository.findByIdAndStudentUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy CV", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        resume.setDeletedAt(LocalDateTime.now());
        resume.setIsDefault(false);
        studentResumeRepository.save(resume);
    }

    @Transactional
    public ProfileDtos.ResumeResponse setDefaultResume(User user, Long id) {
        requireStudent(user);
        StudentResume resume = studentResumeRepository.findByIdAndStudentUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy CV", HttpStatus.NOT_FOUND, "NOT_FOUND"));
        clearDefaultResumes(user.getId());
        resume.setIsDefault(true);
        return toResumeResponse(studentResumeRepository.save(resume));
    }

    private StudentProfile requireStudent(User user) {
        if (!"STUDENT".equals(user.getRole().getCode())) {
            throw new AppException("Chức năng chỉ dành cho sinh viên", HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
        return studentProfileRepository.findById(user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ sinh viên", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    private void clearDefaultResumes(Long studentId) {
        List<StudentResume> defaults = studentResumeRepository
                .findByStudentUserIdAndIsDefaultTrueAndDeletedAtIsNull(studentId);
        for (StudentResume item : defaults) {
            item.setIsDefault(false);
        }
        studentResumeRepository.saveAll(defaults);
    }

    private ProfileDtos.ResumeResponse toResumeResponse(StudentResume resume) {
        return new ProfileDtos.ResumeResponse(
                resume.getId(), resume.getTitle(), resume.getFileUrl(), resume.getFileName(),
                resume.getIsDefault(), resume.getCreatedAt());
    }

    private String provinceName(Short provinceId) {
        if (provinceId == null) {
            return null;
        }
        return provinceRepository.findById(provinceId).map(Province::getName).orElse(null);
    }

    private String defaultFileName(String url) {
        if (url == null || url.isBlank()) {
            return "cv";
        }
        String cleaned = url.split("\\?")[0];
        String[] parts = cleaned.split("/");
        return parts[parts.length - 1].isBlank() ? "cv" : parts[parts.length - 1];
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
