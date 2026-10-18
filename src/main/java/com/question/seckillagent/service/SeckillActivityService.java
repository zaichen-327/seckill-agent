package com.question.seckillagent.service;

import com.question.seckillagent.entity.SeckillActivity;

import java.util.List;

public interface SeckillActivityService {
    List<SeckillActivity> list();

    SeckillActivity getById(Long id);
}
