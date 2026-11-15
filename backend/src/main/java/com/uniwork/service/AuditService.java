package com.uniwork.service;

import com.uniwork.entity.AuditLog;
import com.uniwork.entity.User;
import com.uniwork.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(User actor, String action, String entityType, Long entityId, String ipAddress) {
        auditLogRepository.save(AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress)
                .build());
    }
}
