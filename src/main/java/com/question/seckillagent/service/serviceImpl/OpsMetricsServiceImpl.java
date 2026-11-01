package com.question.seckillagent.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.question.seckillagent.dto.ops.OpsMetricsDTO;
import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.entity.SeckillOrder;
import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.mapper.SeckillOrderMapper;
import com.question.seckillagent.service.OpsMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
// 找到了一个可以不用写改造函数的注解
@RequiredArgsConstructor
public class OpsMetricsServiceImpl implements OpsMetricsService {

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String ORDER_QUEUE = "seckill.order.queue";
    private static final String ORDER_DLQ = "seckill.order.dlq";

    private final SeckillActivityMapper activityMapper;
    private final SeckillOrderMapper orderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final AmqpAdmin amqpAdmin;

    @Override
    public OpsMetricsDTO getMetrics() {
        OpsMetricsDTO dto = new OpsMetricsDTO();
        dto.setTimestamp(LocalDateTime.now());
        dto.setActivities(buildActivityMetrics());
        dto.setMq(buildMqMetrics());
        dto.setRedis(buildRedisMetrics());
        log.info("组装运维指标完成，活动数={}", dto.getActivities().size());
        return dto;
    }

    private List<OpsMetricsDTO.ActivityMetrics> buildActivityMetrics() {
        List<SeckillActivity> activities = activityMapper.selectList(null);
        if (activities.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime now = LocalDateTime.now();

        List<Long> ids = new ArrayList<>();
        for (SeckillActivity a : activities) {
            ids.add(a.getId());
        }

        Map<Long, Long> orderCountMap = batchCountOrders(ids); // 1 条 SQL
        Map<Long, Long> stockMap = batchGetStock(ids);         // 1 次 Redis

        List<OpsMetricsDTO.ActivityMetrics> result = new ArrayList<>();
        for (SeckillActivity a : activities) {
            OpsMetricsDTO.ActivityMetrics m = new OpsMetricsDTO.ActivityMetrics();
            m.setActivityId(a.getId());
            m.setName(a.getName());
            m.setStartTime(a.getStartTime());
            m.setEndTime(a.getEndTime());
            if (now.isBefore(a.getStartTime())) {
                m.setStatus("NOT_STARTED");
            } else if (now.isAfter(a.getEndTime())) {
                m.setStatus("ENDED");
            } else {
                m.setStatus("RUNNING");
            }
            m.setRedisStock(stockMap.get(a.getId())); // null = 未预热/读不到
            m.setDbOrderCount(orderCountMap.getOrDefault(a.getId(), 0L));
            result.add(m);
        }
        return result;
    }

    /**
     * 1 条 SQL 批量统计：SELECT activity_id, COUNT(*) AS cnt
     * FROM seckill_order WHERE activity_id IN (...) GROUP BY activity_id
     */
    private Map<Long, Long> batchCountOrders(List<Long> activityIds) {
        List<Map<String, Object>> rows = orderMapper.selectMaps(new QueryWrapper<SeckillOrder>()
                .select("activity_id", "COUNT(*) AS cnt")
                .in("activity_id", activityIds)
                .groupBy("activity_id"));
        Map<Long, Long> map = new HashMap<>();
        for (Map<String, Object> r : rows) {
            Long activityId = ((Number) r.get("activity_id")).longValue();
            Long cnt = ((Number) r.get("cnt")).longValue();
            map.put(activityId, cnt);
        }
        return map;
    }

    /** 1 次 mget 批量拿库存，返回值与 key 一一对应，缺 key 的位置是 null */
    private Map<Long, Long> batchGetStock(List<Long> activityIds) {
        List<String> keys = new ArrayList<>();
        for (Long id : activityIds){
            keys.add(STOCK_KEY_PREFIX + id);
        }
        try {
            List<String> vals = stringRedisTemplate.opsForValue().multiGet(keys);
            Map<Long, Long> map = new HashMap<>();
            for (int i = 0; i < activityIds.size(); i++) {
                String v = vals.get(i);
                if (v != null) {
                    map.put(activityIds.get(i), Long.parseLong(v));
                }
            }
            return map;
        } catch (Exception e) {
            log.warn("批量读取活动库存失败", e);
            return Collections.emptyMap();
        }
    }

    private OpsMetricsDTO.MqMetrics buildMqMetrics() {
        OpsMetricsDTO.MqMetrics mq = new OpsMetricsDTO.MqMetrics();
        mq.setOrderQueueDepth(queueDepth(ORDER_QUEUE));
        mq.setDlqDepth(queueDepth(ORDER_DLQ));
        return mq;
    }

    private Integer queueDepth(String queueName) {
        try {
            Properties props = amqpAdmin.getQueueProperties(queueName);
            if (props == null) {
                return null;
            }
            return (Integer) props.get("QUEUE_MESSAGE_COUNT");
        } catch (Exception e) {
            log.warn("读取队列积压数失败，queue={}", queueName, e);
            return null;
        }
    }

    private OpsMetricsDTO.RedisMetrics buildRedisMetrics() {
        OpsMetricsDTO.RedisMetrics redis = new OpsMetricsDTO.RedisMetrics();
        try {
            String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
            redis.setUp("PONG".equalsIgnoreCase(pong));
        } catch (Exception e) {
            log.warn("Redis 连通性检查失败", e);
            redis.setUp(false);
        }
        return redis;
    }
}