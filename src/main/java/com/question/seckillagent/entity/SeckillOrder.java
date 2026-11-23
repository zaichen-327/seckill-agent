package com.question.seckillagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.question.seckillagent.domain.order.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long userId;

    private OrderStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
