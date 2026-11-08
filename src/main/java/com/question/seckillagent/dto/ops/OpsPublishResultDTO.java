package com.question.seckillagent.dto.ops;

import lombok.Data;

@Data
public class OpsPublishResultDTO {
    private Long activityId;
    private Integer stock;
    private String status;
}
