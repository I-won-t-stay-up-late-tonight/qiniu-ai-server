package com.qiniuai.chat.web.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @ClassName ApiTimeAspect
 * @Description 接口时间测试
 * @Author IFundo
 * @Date 10:29 2025/9/25
 * @Version 1.0
 */


@Aspect
@Component
public class ApiTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiTimeAspect.class);

    // 修正：第二个参数为 long（基本类型）
    @Pointcut("execution(public * com.qiniuai.chat.web.controller.AudioController..*(..))")
    public void audioChatPointcut() {}

    @Around("audioChatPointcut()")
    public Object calculateAudioChatTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;

            // 获取完整请求路径
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String requestUrl = request.getRequestURI();

            // 获取方法名
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String methodName = methodSignature.getMethod().getName();

            // 输出日志（包含完整路径）
            log.debug("接口 {} 执行完成！方法名：{}，耗时：{} 毫秒",
                    requestUrl, methodName, costTime);

            return result;

        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String requestUrl = request.getRequestURI();

            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            String methodName = methodSignature.getMethod().getName();

            log.error("接口 {} 执行异常！方法名：{}，耗时：{} 毫秒，异常信息：{}",
                    requestUrl, methodName, costTime, e.getMessage());
            throw e;
        }
    }
}