package com.question.seckillagent.service.serviceImpl;

import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.service.SeckillActivityService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillActivityServiceImpl implements SeckillActivityService {
    private final SeckillActivityMapper seckillActivityMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public SeckillActivityServiceImpl(SeckillActivityMapper seckillActivityMapper, StringRedisTemplate stringRedisTemplate){
        this.seckillActivityMapper = seckillActivityMapper;
        this.stringRedisTemplate = stringRedisTemplate;
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

    @Override
    public void publish(Long id) {
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if(activity == null){
            throw new BusinessException("活动不存在");
        }
        stringRedisTemplate.opsForValue().set("seckill:stock:" + id, String.valueOf(activity.getAvailableStock()));
    }
}
