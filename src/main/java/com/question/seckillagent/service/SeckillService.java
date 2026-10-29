package com.question.seckillagent.service;

import java.util.Map;

public interface SeckillService {
    Map<String, String> generateCaptcha(Long activityId);

    String generateToken(Long activityId,String captchaId, String verifyCode);

    Long stockDeduct(Long activityId);

    void doSeckill(Long activityId, String token, Long userId);

    String getSeckillResult(Long activityId, Long userId);
}
