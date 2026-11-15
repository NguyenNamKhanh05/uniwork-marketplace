package com.uniwork.service;

import com.uniwork.entity.EmployerProfile;
import com.uniwork.entity.StudentProfile;
import com.uniwork.entity.User;
import com.uniwork.repository.EmployerProfileRepository;
import com.uniwork.repository.StudentProfileRepository;
import com.uniwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDisplayService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public String displayName(User user) {
        if (user == null) {
            return "";
        }
        String role = user.getRole() != null ? user.getRole().getCode() : "";
        if ("STUDENT".equals(role)) {
            return studentProfileRepository.findById(user.getId())
                    .map(StudentProfile::getFullName)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse(user.getEmail());
        }
        if ("EMPLOYER".equals(role)) {
            return employerProfileRepository.findById(user.getId())
                    .map(EmployerProfile::getDisplayName)
                    .filter(name -> name != null && !name.isBlank())
                    .orElse(user.getEmail());
        }
        return user.getEmail();
    }

    public String displayName(Long userId) {
        return userRepository.findById(userId).map(this::displayName).orElse("");
    }
}
