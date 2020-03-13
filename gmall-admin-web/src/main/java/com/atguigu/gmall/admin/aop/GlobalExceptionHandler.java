package com.atguigu.gmall.admin.aop;


import com.atguigu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一所有的异常处理，给前端返回500的json
 */

@Slf4j
//@ControllerAdvice //添加该注解，代表当前类是一个异常处理类，只要所有的controller有异常都会来这个类
//@ResponseBody
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ArithmeticException.class})
    public Object handlerException(Exception exception){
        log.error("系统全局异常感知，信息：{}",exception.getStackTrace());
        return new CommonResult().validateFailed("数学没有学好");
    }


    @ExceptionHandler(value = {NullPointerException.class})
    public Object handlerException02(Exception exception){
        log.error("系统全局异常感知，信息：{}",exception.getMessage());
        return new CommonResult().validateFailed("空指针了.....");
    }



}
