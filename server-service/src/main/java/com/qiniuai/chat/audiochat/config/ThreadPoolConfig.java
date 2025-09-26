package com.qiniuai.chat.audiochat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类，为不同任务类型创建专用线程池
 */
@Configuration
public class ThreadPoolConfig {

    // ASR线程池配置
    @Value("${thread-pool.asr.core-pool-size}")
    private int asrCorePoolSize;
    @Value("${thread-pool.asr.max-pool-size}")
    private int asrMaxPoolSize;
    @Value("${thread-pool.asr.queue-capacity}")
    private int asrQueueCapacity;
    @Value("${thread-pool.asr.keep-alive-seconds}")
    private int asrKeepAliveSeconds;
    @Value("${thread-pool.asr.thread-name-prefix}")
    private String asrThreadNamePrefix;

    // LLM线程池配置
    @Value("${thread-pool.llm.core-pool-size}")
    private int llmCorePoolSize;
    @Value("${thread-pool.llm.max-pool-size}")
    private int llmMaxPoolSize;
    @Value("${thread-pool.llm.queue-capacity}")
    private int llmQueueCapacity;
    @Value("${thread-pool.llm.keep-alive-seconds}")
    private int llmKeepAliveSeconds;
    @Value("${thread-pool.llm.thread-name-prefix}")
    private String llmThreadNamePrefix;

    // TTS线程池配置
    @Value("${thread-pool.tts.core-pool-size}")
    private int ttsCorePoolSize;
    @Value("${thread-pool.tts.max-pool-size}")
    private int ttsMaxPoolSize;
    @Value("${thread-pool.tts.queue-capacity}")
    private int ttsQueueCapacity;
    @Value("${thread-pool.tts.keep-alive-seconds}")
    private int ttsKeepAliveSeconds;
    @Value("${thread-pool.tts.thread-name-prefix}")
    private String ttsThreadNamePrefix;

    // 语音转换线程池配置
    @Value("${thread-pool.voice-conversion.core-pool-size}")
    private int voiceConversionCorePoolSize;
    @Value("${thread-pool.voice-conversion.max-pool-size}")
    private int voiceConversionMaxPoolSize;
    @Value("${thread-pool.voice-conversion.queue-capacity}")
    private int voiceConversionQueueCapacity;
    @Value("${thread-pool.voice-conversion.keep-alive-seconds}")
    private int voiceConversionKeepAliveSeconds;
    @Value("${thread-pool.voice-conversion.thread-name-prefix}")
    private String voiceConversionThreadNamePrefix;

    /**
     * 语音识别(ASR)专用线程池
     */
    @Bean("asrExecutor")
    public Executor asrExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asrCorePoolSize);
        executor.setMaxPoolSize(asrMaxPoolSize);
        executor.setQueueCapacity(asrQueueCapacity);
        executor.setKeepAliveSeconds(asrKeepAliveSeconds);
        executor.setThreadNamePrefix(asrThreadNamePrefix);
        // 当线程池满时，直接在提交任务的线程中执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * LLM处理专用线程池
     */
    @Bean("llmExecutor")
    public Executor llmExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(llmCorePoolSize);
        executor.setMaxPoolSize(llmMaxPoolSize);
        executor.setQueueCapacity(llmQueueCapacity);
        executor.setKeepAliveSeconds(llmKeepAliveSeconds);
        executor.setThreadNamePrefix(llmThreadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 语音合成(TTS)专用线程池
     */
    @Bean("ttsExecutor")
    public Executor ttsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ttsCorePoolSize);
        executor.setMaxPoolSize(ttsMaxPoolSize);
        executor.setQueueCapacity(ttsQueueCapacity);
        executor.setKeepAliveSeconds(ttsKeepAliveSeconds);
        executor.setThreadNamePrefix(ttsThreadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 语音转换专用线程池
     */
    @Bean("voiceConversionExecutor")
    public Executor voiceConversionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(voiceConversionCorePoolSize);
        executor.setMaxPoolSize(voiceConversionMaxPoolSize);
        executor.setQueueCapacity(voiceConversionQueueCapacity);
        executor.setKeepAliveSeconds(voiceConversionKeepAliveSeconds);
        executor.setThreadNamePrefix(voiceConversionThreadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
    