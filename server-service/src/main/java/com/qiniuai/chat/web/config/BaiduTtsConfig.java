package com.qiniuai.chat.web.config;

import com.baidu.aip.speech.AipSpeech;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 百度智能云TTS配置类
 */
@Configuration
public class BaiduTtsConfig {

    @Value("${baidu.tts.app-id}")
    private String appId;

    @Value("${baidu.tts.api-key}")
    private String apiKey;

    @Value("${baidu.tts.secret-key}")
    private String secretKey;

    /**
     * 初始化百度TTS客户端
     */
    @Bean
    public AipSpeech aipSpeech() {
        // 初始化一个AipSpeech
        AipSpeech client = new AipSpeech(appId, apiKey, secretKey);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        return client;
    }
}
