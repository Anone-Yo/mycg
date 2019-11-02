package com.anone.framework.exception;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局处理异常
 */
@ControllerAdvice//捕获requestmapping 请求的异常
public class BaseExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result exceptionHandler(Exception e) {
        e.printStackTrace();
        return new Result(false, StatusCode.REMOTEERROR,e.getMessage());
    }
}
