package com.question.seckillagent.controller;

import com.question.seckillagent.common.Result;
import com.question.seckillagent.dto.ops.OpsMetricsDTO;
import com.question.seckillagent.service.OpsMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ops")
@RequiredArgsConstructor
public class OpsController {

    private final OpsMetricsService opsMetricsService;

    /**
     * 运维指标总览：Agent 巡检的"眼睛"，一次看清活动 / MQ / Redis 状态
     * Agent 定时拉取：正常不动、死信积压触发排查、库存对不上告警运营
     */
    @GetMapping("/metrics")
    public Result<OpsMetricsDTO> metrics() {
        log.info("Agent 拉取运维指标");
        return Result.success(opsMetricsService.getMetrics());
    }
}
