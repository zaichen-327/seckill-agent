package com.question.seckillagent.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.question.seckillagent.domain.order.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MyOrderDTO {

    private Long orderId;

    private Long activityId;

    private String activityName;

    private OrderStatus status;

    private String statusLabel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
