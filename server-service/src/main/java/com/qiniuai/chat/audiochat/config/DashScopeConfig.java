package com.qiniuai.chat.audiochat.config;
import com.alibaba.dashscope.aigc.generation.Generation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope 客户端配置类
 */
@Configuration
@Slf4j
public class DashScopeConfig {

    /**
     * 初始化 Generation 客户端（单例，由Spring管理）
     */
    @Bean
    public Generation dashScopeGeneration() {
        log.info("初始化 DashScope Generation 客户端");
        return new Generation(); // SDK 内部会自动读取配置的 API Key（或环境变量）
    }
}