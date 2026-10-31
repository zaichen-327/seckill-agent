package com.question.seckillagent.controller;

import com.question.seckillagent.common.Result;
import com.question.seckillagent.context.UserContext;
import com.question.seckillagent.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {
    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService){
        this.seckillService = seckillService;
    }

    //秒杀商品时的验证码的生成，根据活动的ID来生成，第一层防刷
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha(@RequestParam Long activityId){
        log.info("生成验证码，活动ID：{}", activityId);
        return Result.success(seckillService.generateCaptcha(activityId));
    }

    //校验验证码，校验之后，生成一个token来隐藏秒杀地址，第二层防刷
    @GetMapping("/token/{activityId}")
    public Result<String> token(@PathVariable Long activityId, @RequestParam String captchaId, @RequestParam String verifyCode){
        Long userId = UserContext.getUserId();
        log.info("获取秒杀token，activityID={}，userId={}", activityId, userId);
        return Result.success(seckillService.generateToken(activityId, captchaId, verifyCode, userId));
    }

    @PostMapping("/doSeckill/{activityId}/{token}")
    public Result<Void> doSeckill(@PathVariable Long activityId, @PathVariable String token){
        Long userId = UserContext.getUserId();
        log.info("秒杀请求，activityId={}，userId={}", activityId, userId);
        seckillService.doSeckill(activityId,token,userId);
        return Result.success();
    }

    @GetMapping("/result/{activityId}")
    public Result<String> getSeckillResult(@PathVariable Long activityId){
        Long userId = UserContext.getUserId();
        log.info("查询秒杀结果，activityId={}，userId={}", activityId, userId);
        return Result.success(seckillService.getSeckillResult(activityId,userId));
    }
}
