package com.question.seckillagent.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.question.seckillagent.domain.audit.AuditAction;
import com.question.seckillagent.entity.AuditLog;
import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.mapper.AuditLogMapper;
import com.question.seckillagent.service.AuditService;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AuditServiceImpl(AuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(Long actorId,
                       String actorRole,
                       AuditAction action,
                       String targetType,
                       Long targetId,
                       Object before,
                       Object after) {
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setActorRole(actorRole);
        log.setAction(action.name());
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setBeforeJson(toJson(before));
        log.setAfterJson(toJson(after));
        auditLogMapper.insert(log);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException("审计日志序列化失败");
        }
    }
}
