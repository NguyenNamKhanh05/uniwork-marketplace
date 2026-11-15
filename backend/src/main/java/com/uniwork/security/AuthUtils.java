package com.uniwork.security;

import com.uniwork.entity.User;
import com.uniwork.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

/**
 * Tiện ích lấy người dùng hiện tại từ SecurityContext.
 */
public final class AuthUtils {

    private AuthUtils() {
    }

    public static User currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AppException("Yêu cầu đăng nhập để thực hiện thao tác này", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
        return user;
    }
}
