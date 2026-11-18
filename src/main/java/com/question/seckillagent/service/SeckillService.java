package com.question.seckillagent.service;

import com.question.seckillagent.dto.MyOrderDTO;

import java.util.List;
import java.util.Map;

public interface SeckillService {
    Map<String, String> generateCaptcha(Long activityId);

    String generateToken(Long activityId,String captchaId, String verifyCode, Long userId);

    Long stockDeduct(Long activityId);

    void doSeckill(Long activityId, String token, Long userId);

    String getSeckillResult(Long activityId, Long userId);

    List<MyOrderDTO> listMyOrders(Long userId);
}
