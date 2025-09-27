package com.qiniuai.chat.audiochat.repository;

import com.qiniuai.chat.audiochat.entity.VoiceMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 语音消息数据访问接口，操作MongoDB
 */
@Repository
public interface VoiceMessageRepository extends MongoRepository<VoiceMessage, String> {

    /**
     * 根据对话ID查询消息列表
     */
    List<VoiceMessage> findByConversationIdOrderByCreateTimeAsc(String conversationId);
    
    /**
     * 根据对话ID和发送者类型查询消息
     */
    List<VoiceMessage> findByConversationIdAndSenderTypeOrderByCreateTimeAsc(String conversationId, String senderType);
    
    /**
     * 根据状态查询消息
     */
    List<VoiceMessage> findByStatus(String status);
}
    