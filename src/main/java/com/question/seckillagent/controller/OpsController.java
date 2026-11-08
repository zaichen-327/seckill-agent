package com.question.seckillagent.controller;

import com.question.seckillagent.common.Result;
import com.question.seckillagent.dto.ops.*;
import com.question.seckillagent.service.OpsMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/activities")
    public Result<List<OpsActivityDTO>> listActivities(){
        log.info("查询活动列表");
        return Result.success(opsMetricsService.listActivities());
    }

    @GetMapping("/orders")
    public Result<List<OpsOrderDTO>> listResult(
            @RequestParam(required = false) Long activityId,
            @RequestParam(defaultValue = "20") Integer limit){
        log.info("查询最近订单，activityId = {}。limit = {}", activityId, limit);
        return Result.success(opsMetricsService.listRecentOrders(activityId, limit));
    }

    @PostMapping("/activities/{id}/publish")
    public Result<OpsPublishResultDTO> publishActivity(
            @PathVariable("id") Long activityId,
            @RequestBody(required = false) OpsPublishRequest request){
        log.info("发布活动，activityId={}", activityId);
        return Result.success(opsMetricsService.publishActivity(activityId, request));
    }
}
