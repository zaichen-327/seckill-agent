package com.question.seckillagent.service.serviceImpl;

import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.service.SeckillActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillActivityServiceImpl implements SeckillActivityService {
    private final SeckillActivityMapper seckillActivityMapper;

    public SeckillActivityServiceImpl(SeckillActivityMapper seckillActivityMapper){
        this.seckillActivityMapper = seckillActivityMapper;
    }

    @Override
    public List<SeckillActivity> list() {
        return seckillActivityMapper.selectList(null);
    }

    @Override
    public SeckillActivity getById(Long id) {
        return seckillActivityMapper.selectById(id);
    }

    @Override
    public void create(SeckillActivity activity) {
        seckillActivityMapper.insert(activity);
    }
}
