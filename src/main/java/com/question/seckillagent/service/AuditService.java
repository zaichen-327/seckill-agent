package com.question.seckillagent.service;

import com.question.seckillagent.domain.audit.AuditAction;

public interface AuditService {

    void record(Long actorId,
                String actorRole,
                AuditAction action,
                String targetType,
                Long targetId,
                Object before,
                Object after);
}
