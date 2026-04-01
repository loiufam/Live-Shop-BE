package com.lyh.user.exception;

import com.lyh.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        // 打印异常日志，方便后端排查（实际项目可以用 @Slf4j 的 log.error）
        log.error("业务异常：{}", e.getMessage());

        // 拦截异常，包装成统一的 Result 对象返回给前端
        return Result.error(e.getMessage());
    }
}
