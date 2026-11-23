package com.question.seckillagent.service.serviceImpl;

import com.question.seckillagent.mapper.SeckillActivityMapper;
import com.question.seckillagent.mapper.SeckillOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeckillOrderQueryTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private SeckillActivityMapper seckillActivityMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SeckillOrderMapper seckillOrderMapper;

    @InjectMocks
    private SeckillServiceImpl seckillService;

    @Test
    void limitDefaultsToTwenty() {
        assertEquals(20, SeckillServiceImpl.normalizeLimit(null));
        assertEquals(20, SeckillServiceImpl.normalizeLimit(0));
        assertEquals(20, SeckillServiceImpl.normalizeLimit(-1));
    }

    @Test
    void limitIsCappedAtFifty() {
        assertEquals(1, SeckillServiceImpl.normalizeLimit(1));
        assertEquals(50, SeckillServiceImpl.normalizeLimit(50));
        assertEquals(50, SeckillServiceImpl.normalizeLimit(999));
    }

    @Test
    void emptyOrdersReturnsEmptyWithoutLookingUpActivities() {
        when(seckillOrderMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertTrue(seckillService.listMyOrders(1L, null, 20).isEmpty());

        verifyNoInteractions(seckillActivityMapper);
    }
}
