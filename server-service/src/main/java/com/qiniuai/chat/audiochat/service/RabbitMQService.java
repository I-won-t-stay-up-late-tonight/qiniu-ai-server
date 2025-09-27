package com.qiniuai.chat.audiochat.service;

import cn.hutool.core.util.IdUtil;
import com.qiniuai.chat.audiochat.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消息服务，负责发送消息到队列
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    // 交换机和路由键配置
    @Value("${rabbitmq.exchanges.task-exchange}")
    private String taskExchange;
    
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
     * 发送音频合并任务
     */
    public void sendAudioMergeTask(Task task) {
        if (task.getId() == null) {
            task.setId(IdUtil.simpleUUID());
        }
        task.setType("AUDIO_MERGE");
        task.setCreateTime(System.currentTimeMillis());
        task.setStatus("PENDING");
        
        log.info("发送音频合并任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        rabbitTemplate.convertAndSend(taskExchange, audioMergeRoutingKey, task);
    }

    /**
     * 发送ASR任务
     */
    public void sendAsrTask(Task task) {
        if (task.getId() == null) {
            task.setId(IdUtil.simpleUUID());
        }
        task.setType("ASR");
        task.setCreateTime(System.currentTimeMillis());
        task.setStatus("PENDING");
        
        log.info("发送ASR任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        rabbitTemplate.convertAndSend(taskExchange, asrTaskRoutingKey, task);
    }

    /**
     * 发送LLM任务
     */
    public void sendLlmTask(Task task) {
        if (task.getId() == null) {
            task.setId(IdUtil.simpleUUID());
        }
        task.setType("LLM");
        task.setCreateTime(System.currentTimeMillis());
        task.setStatus("PENDING");
        
        log.info("发送LLM任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        rabbitTemplate.convertAndSend(taskExchange, llmTaskRoutingKey, task);
    }

    /**
     * 发送TTS任务
     */
    public void sendTtsTask(Task task) {
        if (task.getId() == null) {
            task.setId(IdUtil.simpleUUID());
        }
        task.setType("TTS");
        task.setCreateTime(System.currentTimeMillis());
        task.setStatus("PENDING");
        
        log.info("发送TTS任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        rabbitTemplate.convertAndSend(taskExchange, ttsTaskRoutingKey, task);
    }

    /**
     * 发送语音转换任务
     */
    public void sendVoiceConversionTask(Task task) {
        if (task.getId() == null) {
            task.setId(IdUtil.simpleUUID());
        }
        task.setType("VOICE_CONVERSION");
        task.setCreateTime(System.currentTimeMillis());
        task.setStatus("PENDING");
        
        log.info("发送语音转换任务，任务ID: {}, 对话ID: {}", task.getId(), task.getConversationId());
        rabbitTemplate.convertAndSend(taskExchange, voiceConversionRoutingKey, task);
    }

    /**
     * 发送WebSocket消息任务
     */
    public void sendWebsocketSendTask(Task task) {
        if (task.getId() == null) {
            task.setId(IdUtil.simpleUUID());
        }
        task.setType("WEBSOCKET_SEND");
        task.setCreateTime(System.currentTimeMillis());
        task.setStatus("PENDING");
        
        log.info("发送WebSocket消息任务，任务ID: {}, 用户ID: {}", task.getId(), task.getUserId());
        rabbitTemplate.convertAndSend(taskExchange, websocketSendRoutingKey, task);
    }
}
    