package com.qiniuai.chat.web.annotation;

/**
 * @ClassName MethodTimer
 * @Description TODO
 * @Author IFundo
 * @Date 14:36 2025/9/25
 * @Version 1.0
 */

import java.lang.annotation.*;

/**
 * 方法执行时间统计注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodTimer {
    // 可以添加描述信息
    String description() default "";
}

