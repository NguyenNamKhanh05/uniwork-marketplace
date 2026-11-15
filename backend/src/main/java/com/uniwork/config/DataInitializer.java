package com.uniwork.config;

import com.uniwork.entity.*;
import com.uniwork.repository.*;
import com.uniwork.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Khởi tạo dữ liệu hệ thống thiết yếu (vai trò, quản trị viên) và dữ liệu mẫu
 * phục vụ chạy thử toàn bộ luồng nghiệp vụ (sinh viên, nhà tuyển dụng, tin tuyển dụng).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final SkillRepository skillRepository;
    private final JobPostRepository jobPostRepository;
    private final JobPostSkillRepository jobPostSkillRepository;
    private final JobPostScheduleRepository jobPostScheduleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.initial-email:}")
    private String adminEmail;

    @Value("${app.admin.initial-password:}")
    private String adminPassword;

    @Value("${app.demo-data.enabled:true}")
    private boolean demoEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initAdminUser();
        if (demoEnabled) {
            initDemoData();
        }
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            log.info("Khởi tạo danh sách vai trò mặc định (STUDENT, EMPLOYER, ADMIN)...");
            roleRepository.saveAll(List.of(
                    new Role(null, "STUDENT", "Sinh viên", "Sinh viên tìm việc part-time"),
                    new Role(null, "EMPLOYER", "Nhà tuyển dụng", "Doanh nghiệp hoặc cá nhân đăng tin"),
                    new Role(null, "ADMIN", "Quản trị viên", "Quản trị hệ thống UniWork")
            ));
        }
    }

    private void initAdminUser() {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("Không có cấu hình tài khoản Admin (ADMIN_INITIAL_EMAIL / ADMIN_INITIAL_PASSWORD). Bỏ qua bước tạo Admin.");
            return;
        }
        createUserIfMissing(adminEmail.trim().toLowerCase(), adminPassword.trim(), "ADMIN", null);
    }

    private void initDemoData() {
        List<Province> provinces = initProvinces();
        initDistricts(provinces);
        List<JobCategory> categories = initCategories();
        List<Skill> skills = initSkills();
        if (categories.isEmpty()) {
            return;
        }

        User student = createUserIfMissing("student@uniwork.vn", "Student@123", "STUDENT", "Nguyễn Văn An");
        User student2 = createUserIfMissing("student2@uniwork.vn", "Student@123", "STUDENT", "Trần Thị Bình");
        User employer = createUserIfMissing("employer@uniwork.vn", "Employer@123", "EMPLOYER", "Công ty TNHH Minh An");
        User employer2 = createUserIfMissing("employer2@uniwork.vn", "Employer@123", "EMPLOYER", "Quán Cà phê Sáng Tạo");

        if (student != null && studentSkillRepository.findByStudentId(student.getId()).isEmpty()) {
            List<StudentSkill> studentSkills = new ArrayList<>();
            for (int i = 0; i < Math.min(4, skills.size()); i++) {
                StudentSkill item = new StudentSkill();
                item.setStudentId(student.getId());
                item.setSkillId(skills.get(i).getId());
                item.setSkill(skills.get(i));
                item.setLevel(i % 2 == 0 ? "INTERMEDIATE" : "BEGINNER");
                studentSkills.add(item);
            }
            studentSkillRepository.saveAll(studentSkills);
        }

        if (jobPostRepository.count() == 0 && employer != null) {
            JobPost firstJob = createJob(
                    employer, categories.get(0),
                    "Nhân viên phục vụ quán cà phê cuối tuần",
                    "Phục vụ khách, pha chế đồ uống cơ bản và dọn dẹp quầy bar. Làm việc theo ca cuối tuần.",
                    "Ưu tiên sinh viên nhanh nhẹn, có trách nhiệm. Không cần kinh nghiệm, sẽ được hướng dẫn.",
                    "Lương theo giờ, phụ cấp ăn uống, thưởng chuyên cần.",
                    "ONSITE", provinceId(provinces, 0),
                    new BigDecimal("30000"), new BigDecimal("40000"), (short) 3);
            addSchedule(firstJob, (short) 6, "08:00", "12:00");
            addSchedule(firstJob, (short) 7, "13:00", "17:00");
            addSkill(firstJob, skills, 0);

            JobPost secondJob = createJob(
                    employer2, categories.get(Math.min(1, categories.size() - 1)),
                    "Thiết kế poster và bài đăng mạng xã hội",
                    "Thiết kế ấn phẩm truyền thông cho quán, đăng bài trên fanpage theo lịch tuần.",
                    "Thành thạo Canva hoặc Photoshop, có khiếu thẩm mỹ.",
                    "Trả theo sản phẩm, làm việc từ xa, thời gian linh hoạt.",
                    "REMOTE", provinceId(provinces, 0),
                    new BigDecimal("150000"), new BigDecimal("300000"), (short) 1);
            addSkill(secondJob, skills, Math.min(2, skills.size() - 1));

            JobPost thirdJob = createJob(
                    employer, categories.get(Math.min(2, categories.size() - 1)),
                    "Gia sư Toán cho học sinh lớp 9",
                    "Dạy kèm Toán 2 buổi mỗi tuần cho học sinh lớp 9, kiểm tra bài cũ và giao bài tập.",
                    "Sinh viên ngành Toán, Sư phạm hoặc kỹ thuật, điểm Toán từ 8.0.",
                    "Lương theo giờ, thưởng theo kết quả học tập của học sinh.",
                    "ONSITE", provinceId(provinces, 1),
                    new BigDecimal("80000"), new BigDecimal("120000"), (short) 2);
            addSchedule(thirdJob, (short) 2, "18:00", "20:00");
            addSchedule(thirdJob, (short) 5, "18:00", "20:00");
            addSkill(thirdJob, skills, Math.min(1, skills.size() - 1));

            createJob(
                    employer, categories.get(Math.min(3, categories.size() - 1)),
                    "Cộng tác viên nhập liệu bán thời gian",
                    "Nhập liệu đơn hàng và kiểm tra thông tin khách hàng trên hệ thống nội bộ.",
                    "Gõ máy nhanh, cẩn thận, biết Excel cơ bản.",
                    "Làm việc tại văn phòng, hỗ trợ máy tính và nước uống.",
                    "ONSITE", provinceId(provinces, 0),
                    new BigDecimal("25000"), new BigDecimal("35000"), (short) 4);

            createJob(
                    employer, categories.get(Math.min(4, categories.size() - 1)),
                    "Chạy thử nghiệm và báo lỗi ứng dụng web",
                    "Sử dụng ứng dụng theo kịch bản, ghi nhận lỗi và mô tả các bước tái hiện.",
                    "Hiểu cơ bản về trình duyệt web, viết mô tả rõ ràng.",
                    "Lương theo ca, được hướng dẫn quy trình kiểm thử.",
                    "HYBRID", provinceId(provinces, 0),
                    new BigDecimal("40000"), new BigDecimal("60000"), (short) 2);
        }

        log.info("Dữ liệu mẫu đã sẵn sàng: student@uniwork.vn / Student@123, employer@uniwork.vn / Employer@123");
    }

    private List<Province> initProvinces() {
        if (provinceRepository.count() > 0) {
            return provinceRepository.findAllByOrderByNameAsc();
        }
        log.info("Khởi tạo dữ liệu tỉnh thành mẫu...");
        return provinceRepository.saveAll(List.of(
                Province.builder().code("HN").name("Hà Nội").build(),
                Province.builder().code("HCM").name("TP. Hồ Chí Minh").build(),
                Province.builder().code("DN").name("Đà Nẵng").build(),
                Province.builder().code("HP").name("Hải Phòng").build(),
                Province.builder().code("CT").name("Cần Thơ").build()
        ));
    }

    private void initDistricts(List<Province> provinces) {
        if (districtRepository.count() > 0 || provinces.isEmpty()) {
            return;
        }
        Province hanoi = provinces.get(0);
        districtRepository.saveAll(List.of(
                District.builder().province(hanoi).code("HK").name("Quận Hoàn Kiếm").build(),
                District.builder().province(hanoi).code("CG").name("Quận Cầu Giấy").build(),
                District.builder().province(hanoi).code("TX").name("Quận Thanh Xuân").build()
        ));
    }

    private List<JobCategory> initCategories() {
        if (jobCategoryRepository.count() > 0) {
            return jobCategoryRepository.findAllByOrderBySortOrderAscNameAsc();
        }
        log.info("Khởi tạo danh mục nghề nghiệp mẫu...");
        List<String> names = List.of(
                "Phục vụ quán ăn và cà phê",
                "Thiết kế đồ họa",
                "Gia sư và trợ giảng",
                "Nhập liệu và hành chính",
                "Kiểm thử và hỗ trợ kỹ thuật",
                "Bán hàng và tư vấn",
                "Giao hàng và vận chuyển",
                "Sự kiện và truyền thông"
        );
        List<JobCategory> items = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            items.add(JobCategory.builder()
                    .name(names.get(i))
                    .slug(MasterDataService.slugify(names.get(i)))
                    .sortOrder((short) (i + 1))
                    .isActive(true)
                    .build());
        }
        return jobCategoryRepository.saveAll(items);
    }

    private List<Skill> initSkills() {
        if (skillRepository.count() > 0) {
            return skillRepository.findByIsActiveTrueOrderByNameAsc();
        }
        log.info("Khởi tạo danh sách kỹ năng mẫu...");
        List<String> names = List.of(
                "Giao tiếp", "Phục vụ khách hàng", "Pha chế cơ bản", "Tin học văn phòng",
                "Excel", "Photoshop", "Canva", "Sư phạm", "Toán học", "Tiếng Anh",
                "Kiểm thử phần mềm", "Nhập liệu", "Quản lý thời gian", "Làm việc nhóm"
        );
        List<Skill> items = new ArrayList<>();
        for (String name : names) {
            items.add(Skill.builder()
                    .name(name)
                    .slug(MasterDataService.slugify(name))
                    .isActive(true)
                    .build());
        }
        return skillRepository.saveAll(items);
    }

    private User createUserIfMissing(String email, String password, String roleCode, String displayName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            Role role = roleRepository.findByCode(roleCode).orElse(null);
            if (role == null) {
                return null;
            }
            log.info("Khởi tạo tài khoản mẫu: {}", email);
            User user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(role)
                    .status("ACTIVE")
                    .emailVerifiedAt(LocalDateTime.now())
                    .authProvider("LOCAL")
                    .build();
            user = userRepository.save(user);

            if ("STUDENT".equals(roleCode)) {
                studentProfileRepository.save(StudentProfile.builder()
                        .user(user)
                        .fullName(displayName)
                        .phone("0901234567")
                        .university("Đại học Bách khoa Hà Nội")
                        .major("Công nghệ thông tin")
                        .yearOfStudy((short) 3)
                        .bio("Sinh viên năm 3, có thể làm việc cuối tuần và buổi tối.")
                        .build());
            } else if ("EMPLOYER".equals(roleCode)) {
                employerProfileRepository.save(EmployerProfile.builder()
                        .user(user)
                        .employerType("COMPANY")
                        .displayName(displayName)
                        .contactName(displayName)
                        .contactPhone("0912345678")
                        .description("Đơn vị tuyển dụng uy tín, môi trường làm việc thân thiện.")
                        .address("Hà Nội")
                        .isVerified(true)
                        .verifiedAt(LocalDateTime.now())
                        .build());
            }
            return user;
        });
    }

    private JobPost createJob(User employer, JobCategory category, String title, String description,
                              String requirements, String benefits, String workMode, Short provinceId,
                              BigDecimal salaryMin, BigDecimal salaryMax, Short positions) {
        EmployerProfile profile = employerProfileRepository.findById(employer.getId()).orElse(null);
        if (profile == null) {
            return null;
        }
        JobPost job = JobPost.builder()
                .employer(profile)
                .category(category)
                .title(title)
                .description(description)
                .requirements(requirements)
                .benefits(benefits)
                .workMode(workMode)
                .provinceId(provinceId)
                .addressDetail("Hà Nội")
                .salaryType("HOURLY")
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .positionsCount(positions)
                .positionsFilled((short) 0)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusMonths(3))
                .applicationDeadline(LocalDateTime.now().plusDays(30))
                .status("OPEN")
                .publishedAt(LocalDateTime.now())
                .viewCount(0)
                .build();
        return jobPostRepository.save(job);
    }

    private void addSchedule(JobPost job, Short dayOfWeek, String start, String end) {
        if (job == null) {
            return;
        }
        jobPostScheduleRepository.save(JobPostSchedule.builder()
                .jobPost(job)
                .dayOfWeek(dayOfWeek)
                .startTime(java.time.LocalTime.parse(start))
                .endTime(java.time.LocalTime.parse(end))
                .build());
    }

    private void addSkill(JobPost job, List<Skill> skills, int index) {
        if (job == null || skills.isEmpty()) {
            return;
        }
        Skill skill = skills.get(Math.min(index, skills.size() - 1));
        jobPostSkillRepository.save(JobPostSkill.builder()
                .jobPostId(job.getId())
                .skillId(skill.getId())
                .skill(skill)
                .build());
    }

    private Short provinceId(List<Province> provinces, int index) {
        if (provinces.isEmpty()) {
            return null;
        }
        return provinces.get(Math.min(index, provinces.size() - 1)).getId();
    }
}
