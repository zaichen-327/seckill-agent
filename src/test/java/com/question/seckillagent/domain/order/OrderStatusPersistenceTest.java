package com.question.seckillagent.domain.order;

import com.question.seckillagent.entity.SeckillOrder;
import com.question.seckillagent.mapper.SeckillOrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class OrderStatusPersistenceTest {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Test
    void orderStatusRoundTripsThroughMyBatis() {
        SeckillOrder order = new SeckillOrder();
        order.setActivityId(999L);
        order.setUserId(System.nanoTime());
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        seckillOrderMapper.insert(order);

        SeckillOrder loaded = seckillOrderMapper.selectById(order.getId());
        assertEquals(OrderStatus.PENDING_PAYMENT, loaded.getStatus());
    }
}
