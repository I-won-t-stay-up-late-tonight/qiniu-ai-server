package com.qiniuai.chat.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 验证码配置类
 */
@Component
@ConfigurationProperties(prefix = "verify-code")
@Data
public class VerifyCodeConfig {

    private int length;             // 验证码长度
    private int expireTime;         // 验证码有效期(分钟)
    private int sendInterval;       // 短信发送间隔(秒)
    private int maxDailyCount;      // 每日最大发送次数
}
