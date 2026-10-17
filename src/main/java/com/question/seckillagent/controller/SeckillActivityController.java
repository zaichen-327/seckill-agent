package com.question.seckillagent.controller;

import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.service.SeckillActivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity")
public class SeckillActivityController {

    private final SeckillActivityService seckillActivityService;

    public SeckillActivityController(SeckillActivityService seckillActivityService){
        this.seckillActivityService = seckillActivityService;
    }

    @GetMapping("/list")
    public List<SeckillActivity> list(){
        return seckillActivityService.list();
    }
}
