package com.question.seckillagent.common;

import com.question.seckillagent.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleBusinessException(BusinessException e){
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }
}
