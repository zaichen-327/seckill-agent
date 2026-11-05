package com.question.seckillagent.dto.ops;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OpsActivityDTO {
    private Long id;
    private String name;
    private Long productId;
    private BigDecimal seckillPrice;
    private BigDecimal originalPrice;
    private Integer initialStock;
    private Integer availableStock;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String status;
}
