package com.question.seckillagent.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.question.seckillagent.config.RabbitMQConfig;
import com.question.seckillagent.entity.SeckillOrder;
import com.question.seckillagent.mapper.SeckillOrderMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SeckillOrderConsumer {

    private final SeckillOrderMapper seckillOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public SeckillOrderConsumer(SeckillOrderMapper seckillOrderMapper,
                                StringRedisTemplate stringRedisTemplate,
                                ObjectMapper objectMapper) {
        this.seckillOrderMapper = seckillOrderMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        SeckillMessage seckillMessage;
        try {
            seckillMessage = objectMapper.readValue(message.getBody(), SeckillMessage.class);
        } catch (Exception e) {
            // 毒消息：永远解析不出来，重试多少次都没用，直接拒绝进死信
            log.error("消息无法解析，拒绝进死信，body={}",
                    new String(message.getBody(), StandardCharsets.UTF_8), e);
            channel.basicNack(deliveryTag, false, false);
            return;
        }
        try {
            SeckillOrder order = new SeckillOrder();
            order.setActivityId(seckillMessage.getActivityId());
            order.setUserId(seckillMessage.getUserId());
            seckillOrderMapper.insert(order);
            channel.basicAck(deliveryTag, false);
            log.info("订单落库成功，activityId={}，userId={}",
                    seckillMessage.getActivityId(), seckillMessage.getUserId());
        } catch (DuplicateKeyException e) {
            stringRedisTemplate.opsForValue().increment("seckill:stock:" + seckillMessage.getActivityId());
            channel.basicAck(deliveryTag, false);
            log.info("重复订单，库存已回补并忽略，activityId={}，userId={}",
                    seckillMessage.getActivityId(), seckillMessage.getUserId());
        } catch (Exception e) {
            // 其它落库失败：同样拒绝进死信，留在死信里等排查补偿，不无限悬挂
            log.error("订单落库失败，拒绝进死信，activityId={}，userId={}",
                    seckillMessage.getActivityId(), seckillMessage.getUserId(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}