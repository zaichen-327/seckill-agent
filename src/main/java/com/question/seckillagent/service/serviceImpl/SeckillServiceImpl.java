package com.question.seckillagent.service.serviceImpl;

import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.service.SeckillService;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    private final StringRedisTemplate stringRedisTemplate;

    public SeckillServiceImpl(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
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
}
