package com.qiniuai.chat.demos.rolechat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 音频分片实体，用于临时存储用户发送的音频分片
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audio_chunks")
public class AudioChunk {

    // 分片ID
    private String id;
    
    // 对话ID
    private String conversationId;
    
    // 用户ID
    private String userId;
    
    // 分片序号
    private int chunkIndex;
    
    // 总分片数
    private int totalChunks;
    
    // 音频数据（Base64编码）
    private String audioData;
    
    // 是否最后一个分片
    private boolean isLastChunk;
    
    // 创建时间戳
    private long createTime;
}
    