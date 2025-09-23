package com.qiniuai.chat.demos.web.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云短信配置类
 */
@Configuration
@ConfigurationProperties(prefix = "tencent.sms")
@Data
public class TencentSmsConfig {

    private String secretId;
    private String secretKey;
    private String smsSdkAppId;
    private String templateId;
    private String signName;

    /**
     * 创建腾讯云短信客户端
     */
    @Bean
    public SmsClient smsClient() {
        // 实例化一个认证对象，入参需要传入腾讯云账户SecretId和SecretKey
        Credential cred = new Credential(secretId, secretKey);
        
        // 实例化要请求产品的client对象,clientProfile是可选的
        return new SmsClient(cred, "ap-guangzhou");
    }
}
