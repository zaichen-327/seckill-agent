package com.question.seckillagent.service.serviceImpl;

import com.question.seckillagent.entity.SeckillActivity;
import com.question.seckillagent.exception.BusinessException;
import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.service.SeckillActivityService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
        //在接口调用成功但是无法返回数据时把信息返回前端，避免活动不存在却返回成功调用的逻辑
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if(activity == null){
            throw new BusinessException("活动不存在");
        }
        return activity;
    }

    @Override
    public void create(SeckillActivity activity) {
        seckillActivityMapper.insert(activity);
        // 测试环境都有，因为测试环境需要频繁重启测试新功能，测试活动基本都是发布在同一个id之下，所以要清除redis缓存
        // 生产环境里面一般不会出现这个情况
        stringRedisTemplate.delete("seckill:stock:" + activity.getId());
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
