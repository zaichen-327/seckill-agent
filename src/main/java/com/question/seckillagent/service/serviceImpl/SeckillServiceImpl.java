package com.question.seckillagent.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.question.seckillagent.config.RabbitMQConfig;
import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.entity.SeckillOrder;
import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.mapper.SeckillOrderMapper;
import com.question.seckillagent.mq.SeckillMessage;
import com.question.seckillagent.service.SeckillService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> seckillScript;
    private final SeckillActivityMapper seckillActivityMapper;
    private final RabbitTemplate rabbitTemplate;
    private final SeckillOrderMapper seckillOrderMapper;

    public SeckillServiceImpl(StringRedisTemplate stringRedisTemplate, SeckillActivityMapper seckillActivityMapper, RabbitTemplate rabbitTemplate, SeckillOrderMapper seckillOrderMapper){
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillActivityMapper = seckillActivityMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.seckillOrderMapper = seckillOrderMapper;
        this.seckillScript = new DefaultRedisScript<>();
        this.seckillScript.setLocation(new ClassPathResource("lua/seckill.lua"));
        this.seckillScript.setResultType(Long.class);
    }

    @PostConstruct
    public void initRabbitCallbacks() {
        // 消息没被交换机确认收到：说明没发出去，回补库存
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息未被确认，correlationData={}，原因={}",
                        correlationData == null ? null : correlationData.getId(), cause);
                rollbackStock(correlationData == null ? null : correlationData.getId());
            }
        });
        // 消息到了交换机但路由不到队列：被退回，同样回补库存
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息被退回，exchange={}，routingKey={}，replyText={}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText());
            rollbackStock(returned.getMessage().getMessageProperties().getCorrelationId());
        });
    }

    private void rollbackStock(String correlationId) {
        if (correlationId == null) {
            return;
        }
        Long activityId = Long.valueOf(correlationId.split(":")[0]);
        stringRedisTemplate.opsForValue().increment("seckill:stock:" + activityId);
        log.warn("发送失败，库存已回补，activityId={}", activityId);
    }

    @Override
    public String generateCaptcha(Long activityId) {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48, 2);
        String answer = captcha.text();
        stringRedisTemplate.opsForValue().set("seckill:captcha:" + activityId, answer, 120, TimeUnit.SECONDS);
        log.info("生成验证码，activityID={}，答案已经存入Redis", activityId);
        return captcha.toBase64();
    }

    @Override
    public String generateToken(Long activityId, String verifyCode) {
        String answer = stringRedisTemplate.opsForValue().get("seckill:captcha:" + activityId);
        if(answer == null || !answer.equals(verifyCode.trim())){
            throw new BusinessException("验证码错误");
        }
        stringRedisTemplate.delete("seckill:captcha:" + activityId);
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set("seckill:token:" + activityId, token, 60, TimeUnit.SECONDS);
        log.info("生成秒杀token，activityId={}", activityId);
        return token;
    }

    @Override
    public Long stockDeduct(Long activityId) {
        Long result = stringRedisTemplate.execute(seckillScript, Collections.singletonList("seckill:stock:" + activityId));
        log.info("Lua原子扣库存，activityId = {}，返回值 = {}", activityId, result);
        return result;
    }

    private void checkRateLimit(Long activityId, Long userId){
        String key = "seckill:limit:" + activityId + ":" + userId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if(count != null && count == 1){
            stringRedisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        if(count != null && count > 10){
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
    }

    @Override
    public void doSeckill(Long activityId, String token, Long userId) {
        // 直接在源头进行脚本刷单检测，如果是机器刷单，直接略过token检验这一部分，拒绝刷单请求
        checkRateLimit(activityId, userId);
        // 1. 校验token：先删后用，防重放
        String tokenKey = "seckill:token:" + activityId;
        String savedToken = stringRedisTemplate.opsForValue().get(tokenKey);
        if (savedToken == null || !savedToken.equals(token)) {
            throw new BusinessException("token无效或已过期");
        }
        stringRedisTemplate.delete(tokenKey);

        // 2. 校验活动时间窗
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束");
        }

        // 3. Lua原子扣库存
        //每人限购一份
        Long result = stockDeduct(activityId);
        if (result == -1) {
            log.warn("活动未预热,activityId={}",activityId);
            throw new BusinessException("活动未开始");
        }
        if (result == -2) {
            throw new BusinessException("已售罄");
        }

        // 4. 发消息异步落单，热点路径到此结束
        SeckillMessage message = new SeckillMessage(activityId, userId);
        String correlationId = activityId + ":" + userId;
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    },
                    new CorrelationData(correlationId));
        } catch (AmqpException e) {
            log.error("消息发送失败，activityId={}，userId={}", activityId, userId, e);
            rollbackStock(correlationId);
            throw new BusinessException("秒杀太火爆了，请稍后再试");
        }
        log.info("秒杀请求已入队，activityId={}，userId={}", activityId, userId);
    }

    @Override
    public String getSeckillResult(Long activityId, Long userId) {
        // 1. 先查订单：有订单 = 秒杀成功
        QueryWrapper<SeckillOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("user_id", userId);
        SeckillOrder order = seckillOrderMapper.selectOne(wrapper);
        if (order != null) {
            return "秒杀成功";
        }
        // 2. 再看库存：还有库存 = 还在排队；库存空了 = 已售罄
        String stock = stringRedisTemplate.opsForValue().get("seckill:stock:" + activityId);
        if (stock == null) {
            return "活动未开始";
        }
        if (Long.parseLong(stock) > 0) {
            return "排队中";
        }
        return "已售罄";
    }
}
