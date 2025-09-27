package com.qiniuai.chat.audiochat.repository;

import com.qiniuai.chat.audiochat.entity.AudioChunk;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 音频分片数据访问接口，操作MongoDB
 */
@Repository
public interface ChatAudioChunkRepository extends MongoRepository<AudioChunk, String> {

    /**
     * 根据对话ID查询所有分片
     */
    List<AudioChunk> findByConversationIdOrderByChunkIndexAsc(String conversationId);
    
    /**
     * 根据对话ID和用户ID查询分片
     */
    List<AudioChunk> findByConversationIdAndUserIdOrderByChunkIndexAsc(String conversationId, String userId);
    
    /**
     * 删除对话的所有分片
     */
    void deleteByConversationId(String conversationId);
}
    