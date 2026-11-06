package com.question.seckillagent.dto.ops;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OpsOrderDTO {
    private Long id;
    private Long userId;
    private Long activityId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}