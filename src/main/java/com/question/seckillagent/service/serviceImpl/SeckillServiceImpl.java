package com.question.seckillagent.service.serviceImpl;

import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.entity.SeckillOrder;
import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.mapper.SeckillOrderMapper;
import com.question.seckillagent.service.SeckillService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

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
    private final SeckillOrderMapper seckillOrderMapper;

    public SeckillServiceImpl(StringRedisTemplate stringRedisTemplate, SeckillActivityMapper seckillActivityMapper, SeckillOrderMapper seckillOrderMapper){
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillActivityMapper = seckillActivityMapper;
        this.seckillOrderMapper = seckillOrderMapper;
        this.seckillScript = new DefaultRedisScript<>();
        this.seckillScript.setLocation(new ClassPathResource("lua/seckill.lua"));
        this.seckillScript.setResultType(Long.class);
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

    @Override
    public void doSeckill(Long activityId, String token, Long userId) {
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

        // 4. 插入订单：唯一键防重单，冲突时回补库存
        SeckillOrder order = new SeckillOrder();
        order.setActivityId(activityId);
        order.setUserId(userId);
        try {
            seckillOrderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            stringRedisTemplate.opsForValue().increment("seckill:stock:" + activityId);
            log.info("重复抢购拦截，activityId={}，userId={}，库存已回补", activityId, userId);
            throw new BusinessException("您已秒杀成功，请勿重复抢购");
        }
        log.info("秒杀成功，activityId={}，userId={}", activityId, userId);
    }
}
