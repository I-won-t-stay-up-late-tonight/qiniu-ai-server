package com.qiniuai.chat.audiochat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 语音消息实体，存储在MongoDB中
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "voice_messages")
public class VoiceMessage {

    @Id
    private String id;
    
    // 对话ID
    private String conversationId;
    
    // 发送者ID（用户ID或角色ID）
    private String senderId;
    
    // 发送者类型（USER或ROLE）
    private String senderType;
    
    // 接收者ID
    private String receiverId;
    
    // 语音文件路径
    private String voiceFilePath;
    
    // 语音时长（秒）
    private double duration;
    
    // 语音转文本内容
    private String textContent;
    
    // 消息状态（PENDING, PROCESSING, COMPLETED, FAILED）
    private String status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
}
    