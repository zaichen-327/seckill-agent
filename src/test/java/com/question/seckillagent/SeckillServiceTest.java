package com.question.seckillagent;

import com.question.seckillagent.service.SeckillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SeckillServiceTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void stockDeduct() {
        Long testId = 999L;
        String key = "seckill:stock:" + testId;
        stringRedisTemplate.opsForValue().set(key, "2");
        assertEquals(1L, seckillService.stockDeduct(testId));
        assertEquals(0L, seckillService.stockDeduct(testId));
        assertEquals(-2L, seckillService.stockDeduct(testId));
        stringRedisTemplate.delete(key);
        assertEquals(-1L, seckillService.stockDeduct(testId));
    }
}