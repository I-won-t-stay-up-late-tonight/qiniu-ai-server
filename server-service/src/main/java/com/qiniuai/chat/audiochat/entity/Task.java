package com.qiniuai.chat.audiochat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 任务实体，用于RabbitMQ消息传递
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    // 任务ID
    private String id;
    
    // 任务类型（AUDIO_MERGE, ASR, LLM, TTS, VOICE_CONVERSION, WEBSOCKET_SEND）
    private String type;
    
    // 对话ID
    private String conversationId;
    
    // 用户ID
    private String userId;
    
    // 角色ID
    private String roleId;
    
    // 任务数据（根据任务类型不同而不同）
    private Map<String, Object> data;
    
    // 任务状态（PENDING, PROCESSING, COMPLETED, FAILED）
    private String status;
    
    // 错误信息（如果失败）
    private String errorMessage;
    
    // 创建时间戳
    private long createTime;
    
    // 预计完成时间戳
    private long estimatedCompletionTime;
}
    