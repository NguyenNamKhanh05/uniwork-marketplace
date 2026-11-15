package com.uniwork.repository;

import com.uniwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho bảng `users`.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /** Tìm user theo email (dùng cho login và UserDetailsService) */
    Optional<User> findByEmail(String email);

    /** Kiểm tra email đã tồn tại chưa (dùng khi đăng ký) */
    boolean existsByEmail(String email);

    long countByRoleCode(String roleCode);

    long countByRoleCodeAndDeletedAtIsNull(String roleCode);
}

