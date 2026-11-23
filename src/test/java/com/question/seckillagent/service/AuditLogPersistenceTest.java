package com.question.seckillagent.service;

import com.question.seckillagent.entity.AuditLog;
import com.question.seckillagent.mapper.AuditLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class AuditLogPersistenceTest {

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Test
    void auditLogRoundTripsThroughMyBatis() {
        AuditLog log = new AuditLog();
        log.setActorId(1L);
        log.setActorRole("USER");
        log.setAction("ORDER_CREATED");
        log.setTargetType("ORDER");
        log.setTargetId(100L);
        log.setAfterJson("{\"status\":\"PENDING_PAYMENT\"}");

        auditLogMapper.insert(log);

        AuditLog loaded = auditLogMapper.selectById(log.getId());
        assertEquals("ORDER_CREATED", loaded.getAction());
        assertEquals("ORDER", loaded.getTargetType());
        assertNotNull(loaded.getCreateTime());
    }
}
