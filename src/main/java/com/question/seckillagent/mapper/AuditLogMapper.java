package com.question.seckillagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.question.seckillagent.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
