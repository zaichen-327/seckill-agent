package com.question.seckillagent.dto.ops;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 运维指标总览 DTO，供运维 Agent 定时拉取巡检
 */
@Data
public class OpsMetricsDTO {

    /** 指标采集时间 */
    private LocalDateTime timestamp;

    /** 各活动指标 */
    private List<ActivityMetrics> activities;

    /** MQ 指标 */
    private MqMetrics mq;

    /** Redis 指标 */
    private RedisMetrics redis;

    @Data
    public static class ActivityMetrics {
        private Long activityId;
        private String name;
        /** 活动状态：NOT_STARTED 未开始 / RUNNING 进行中 / ENDED 已结束 */
        private String status;
        /** Redis 剩余库存（null 表示未预热/未发布） */
        private Long redisStock;
        /** DB 已落库订单数 */
        private Long dbOrderCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    @Data
    public static class MqMetrics {
        /** 订单队列积压数 */
        private Integer orderQueueDepth;
        /** 死信队列积压数 */
        private Integer dlqDepth;
    }

    @Data
    public static class RedisMetrics {
        /** Redis 是否连通 */
        private Boolean up;
    }
}
