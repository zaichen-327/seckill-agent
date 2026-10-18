package com.question.seckillagent.controller;

import com.question.seckillagent.common.Result;
import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/activity")
public class SeckillActivityController {

    //构造器注入，测试友好、线程安全
    private final SeckillActivityService seckillActivityService;

    public SeckillActivityController(SeckillActivityService seckillActivityService){
        this.seckillActivityService = seckillActivityService;
    }

    @GetMapping("/list")
    public Result<List<SeckillActivity>> list(){
        log.info("开始查询整个秒杀活动列表");
        return Result.success(seckillActivityService.list());
    }

    @GetMapping("/{id}")
    public Result<SeckillActivity> detail(@PathVariable Long id){
        log.info("根据id查询商品详细：{}", id);
        return Result.success(seckillActivityService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody SeckillActivity activity){
        log.info("创建秒杀活动，名字={}，商品ID={}", activity.getName(), activity.getProductId());
        seckillActivityService.create(activity);
        return Result.success();
    }

    @PostMapping("/publish/{id}")
    public Result<Void> publish(@PathVariable Long id){
        log.info("发布秒杀活动，预热库存：ID={}", id);
        seckillActivityService.publish(id);
        return Result.success();
    }
}
