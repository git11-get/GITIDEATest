package com.atguigu.gmall.admin.aop;


import com.atguigu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Slf4j
@Aspect
@Component
public class DataValiAspect {


    //第一种方式：
    //目标方法的异常：一般都需要再次抛出去，让别感知
    @Around("execution(* com.atguigu.gmall.admin..*Controller.*(..))")
    public Object valiAround(ProceedingJoinPoint point){
        Object proceed = null;
        try{
            log.debug("校验切面介入工作....");
            Object[] args = point.getArgs();
            for (Object obj : args) {
                if(obj instanceof BindingResult){
                    BindingResult r = (BindingResult) obj;
                    if(r.getErrorCount()>0){
                        //框架自动校验检测到错
                        return new CommonResult().validateFailed(r);
                    }
                }
            }
            proceed = point.proceed(point.getArgs());
            log.debug("校验切面将目标方法已放行...{}",proceed);
        }catch (Throwable throwable){
            throw new RuntimeException(throwable);

        }finally {

        }
        return proceed;
    }



    //第二种方式：
    @Around("execution(* com.atguigu.gmall.admin..*Controller.*(..))")
    public Object valiAround02(ProceedingJoinPoint point) throws Throwable {
        Object proceed = null;
        log.debug("校验切面介入工作....");
        Object[] args = point.getArgs();
        for (Object obj : args) {
            if(obj instanceof BindingResult){
                BindingResult r = (BindingResult) obj;
                if(r.getErrorCount()>0){
                    //框架自动校验检测到错
                    return new CommonResult().validateFailed(r);
                }
            }
        }
        proceed = point.proceed(point.getArgs());
        log.debug("校验切面将目标方法已放行...{}",proceed);
        return proceed;
    }


}
