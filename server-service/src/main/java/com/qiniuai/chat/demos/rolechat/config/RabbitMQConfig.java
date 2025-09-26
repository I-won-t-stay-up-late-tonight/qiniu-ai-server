package com.qiniuai.chat.demos.rolechat.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类，定义队列、交换机和绑定关系
 */
@Configuration
public class RabbitMQConfig {

    // 队列名称
    @Value("${rabbitmq.queues.audio-merge}")
    private String audioMergeQueue;
    @Value("${rabbitmq.queues.asr-task}")
    private String asrTaskQueue;
    @Value("${rabbitmq.queues.llm-task}")
    private String llmTaskQueue;
    @Value("${rabbitmq.queues.tts-task}")
    private String ttsTaskQueue;
    @Value("${rabbitmq.queues.voice-conversion-task}")
    private String voiceConversionTaskQueue;
    @Value("${rabbitmq.queues.websocket-send}")
    private String websocketSendQueue;

    // 交换机名称
    @Value("${rabbitmq.exchanges.task-exchange}")
    private String taskExchange;

    // 路由键
    @Value("${rabbitmq.routing-keys.audio-merge}")
    private String audioMergeRoutingKey;
    @Value("${rabbitmq.routing-keys.asr-task}")
    private String asrTaskRoutingKey;
    @Value("${rabbitmq.routing-keys.llm-task}")
    private String llmTaskRoutingKey;
    @Value("${rabbitmq.routing-keys.tts-task}")
    private String ttsTaskRoutingKey;
    @Value("${rabbitmq.routing-keys.voice-conversion-task}")
    private String voiceConversionRoutingKey;
    @Value("${rabbitmq.routing-keys.websocket-send}")
    private String websocketSendRoutingKey;

    /**
     * 声明任务交换机
     */
    @Bean
    public DirectExchange taskExchange() {
        // 持久化、非自动删除
        return ExchangeBuilder.directExchange(taskExchange)
                .durable(true)
                .autoDelete()
                .build();
    }

    /**
     * 音频合并队列
     */
    @Bean
    public Queue audioMergeQueue() {
        return QueueBuilder.durable(audioMergeQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", audioMergeQueue + ".dlq")
                .withArgument("x-message-ttl", 60000) // 1分钟过期
                .build();
    }

    /**
     * ASR任务队列
     */
    @Bean
    public Queue asrTaskQueue() {
        return QueueBuilder.durable(asrTaskQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", asrTaskQueue + ".dlq")
                .withArgument("x-message-ttl", 120000) // 2分钟过期
                .build();
    }

    /**
     * LLM任务队列
     */
    @Bean
    public Queue llmTaskQueue() {
        return QueueBuilder.durable(llmTaskQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", llmTaskQueue + ".dlq")
                .withArgument("x-message-ttl", 150000) // 2.5分钟过期
                .build();
    }

    /**
     * TTS任务队列
     */
    @Bean
    public Queue ttsTaskQueue() {
        return QueueBuilder.durable(ttsTaskQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ttsTaskQueue + ".dlq")
                .withArgument("x-message-ttl", 120000) // 2分钟过期
                .build();
    }

    /**
     * 语音转换任务队列
     */
    @Bean
    public Queue voiceConversionTaskQueue() {
        return QueueBuilder.durable(voiceConversionTaskQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", voiceConversionTaskQueue + ".dlq")
                .withArgument("x-message-ttl", 120000) // 2分钟过期
                .build();
    }

    /**
     * WebSocket发送队列
     */
    @Bean
    public Queue websocketSendQueue() {
        return QueueBuilder.durable(websocketSendQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", websocketSendQueue + ".dlq")
                .withArgument("x-message-ttl", 60000) // 1分钟过期
                .build();
    }

    /**
     * 死信队列 - 处理失败的消息
     */
    @Bean
    public Queue audioMergeDlqQueue() {
        return QueueBuilder.durable(audioMergeQueue + ".dlq").build();
    }

    @Bean
    public Queue asrTaskDlqQueue() {
        return QueueBuilder.durable(asrTaskQueue + ".dlq").build();
    }

    @Bean
    public Queue llmTaskDlqQueue() {
        return QueueBuilder.durable(llmTaskQueue + ".dlq").build();
    }

    @Bean
    public Queue ttsTaskDlqQueue() {
        return QueueBuilder.durable(ttsTaskQueue + ".dlq").build();
    }

    @Bean
    public Queue voiceConversionTaskDlqQueue() {
        return QueueBuilder.durable(voiceConversionTaskQueue + ".dlq").build();
    }

    @Bean
    public Queue websocketSendDlqQueue() {
        return QueueBuilder.durable(websocketSendQueue + ".dlq").build();
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding audioMergeBinding(Queue audioMergeQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(audioMergeQueue)
                .to(taskExchange)
                .with(audioMergeRoutingKey);
    }

    @Bean
    public Binding asrTaskBinding(Queue asrTaskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(asrTaskQueue)
                .to(taskExchange)
                .with(asrTaskRoutingKey);
    }

    @Bean
    public Binding llmTaskBinding(Queue llmTaskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(llmTaskQueue)
                .to(taskExchange)
                .with(llmTaskRoutingKey);
    }

    @Bean
    public Binding ttsTaskBinding(Queue ttsTaskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(ttsTaskQueue)
                .to(taskExchange)
                .with(ttsTaskRoutingKey);
    }

    @Bean
    public Binding voiceConversionTaskBinding(Queue voiceConversionTaskQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(voiceConversionTaskQueue)
                .to(taskExchange)
                .with(voiceConversionRoutingKey);
    }

    @Bean
    public Binding websocketSendBinding(Queue websocketSendQueue, DirectExchange taskExchange) {
        return BindingBuilder.bind(websocketSendQueue)
                .to(taskExchange)
                .with(websocketSendRoutingKey);
    }
}
    