package com.question.seckillagent.service;

import com.question.seckillagent.dto.ops.OpsActivityDTO;
import com.question.seckillagent.dto.ops.OpsMetricsDTO;

import java.util.List;

public interface OpsMetricsService {
    // 组装运维指标总览（活动，MQ，Redis）
    OpsMetricsDTO getMetrics();

    List<OpsActivityDTO> listActivities();
}
