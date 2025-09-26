package com.qiniuai.chat.web.Aspect;

/**
 * @ClassName MethodTimerAspect
 * @Description TODO
 * @Author IFundo
 * @Date 14:37 2025/9/25
 * @Version 1.0
 */

import com.qiniuai.chat.web.annotation.MethodTimer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 方法执行时间统计切面
 */
@Aspect
@Component
public class MethodTimerAspect {
    private static final Logger logger = LoggerFactory.getLogger(MethodTimerAspect.class);

    // 定义切入点，匹配所有带有@MethodTimer注解的方法
    @Pointcut("@annotation(com.qiniuai.chat.web.annotation.MethodTimer)")
    public void methodTimerPointcut() {}

    // 环绕通知，用于统计方法执行时间
    @Around("methodTimerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 执行目标方法
        Object result = joinPoint.proceed();

        // 计算执行时间
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodTimer methodTimer = method.getAnnotation(MethodTimer.class);

        // 日志输出执行时间
        logger.info("Method [{}] {} executed in {} ms",
                joinPoint.getTarget().getClass().getSimpleName(),
                method.getName(),
                executionTime);

        return result;
    }
}

