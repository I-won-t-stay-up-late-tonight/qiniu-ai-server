package com.qiniuai.chat.web.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "xunfei.tts")
@Data
public class XunfeiTtsConfig {

    private String appId;
    private String apiKey;
    private String apiSecret;
    private String hostUrl;
    private Map<String, String> roleMapping;

}
