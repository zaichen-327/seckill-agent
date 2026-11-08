package com.question.seckillagent.service;

import com.question.seckillagent.dto.ops.*;

import java.util.List;

public interface OpsMetricsService {
    // 组装运维指标总览（活动，MQ，Redis）
    OpsMetricsDTO getMetrics();

    List<OpsActivityDTO> listActivities();

    List<OpsOrderDTO> listRecentOrders(Long activityId, Integer limit);

    OpsPublishResultDTO publishActivity(Long activityId, OpsPublishRequest request);
}
