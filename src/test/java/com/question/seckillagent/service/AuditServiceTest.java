package com.question.seckillagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.question.seckillagent.domain.audit.AuditAction;
import com.question.seckillagent.entity.AuditLog;
import com.question.seckillagent.mapper.AuditLogMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditServiceTest {

    @Test
    void recordWritesActorActionTargetAndJson() {
        AuditLogMapper mapper = mock(AuditLogMapper.class);
        AuditService auditService = new AuditService(mapper, new ObjectMapper());

        auditService.record(7L, "USER", AuditAction.ORDER_CREATED, "ORDER", 100L,
                null, Map.of("status", "PENDING_PAYMENT"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(mapper).insert(captor.capture());
        AuditLog log = captor.getValue();

        assertEquals(7L, log.getActorId());
        assertEquals("USER", log.getActorRole());
        assertEquals("ORDER_CREATED", log.getAction());
        assertEquals("ORDER", log.getTargetType());
        assertEquals(100L, log.getTargetId());
        assertTrue(log.getAfterJson().contains("PENDING_PAYMENT"));
    }
}
