package com.question.seckillagent.service;

public interface SeckillService {
    String generateCaptcha(Long activityId);

    String generateToken(Long activityId, String verifyCode);

    Long stockDeduct(Long activityId);

    void doSeckill(Long activityId, String token, Long userId);
}
