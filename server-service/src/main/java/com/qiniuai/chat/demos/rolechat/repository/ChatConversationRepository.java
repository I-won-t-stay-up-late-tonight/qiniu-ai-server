package com.qiniuai.chat.demos.rolechat.repository;

import com.qiniuai.chat.demos.rolechat.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话数据访问接口，操作MongoDB
 */
@Repository
public interface ChatConversationRepository extends MongoRepository<Conversation, String> {

    /**
     * 根据用户ID查询对话列表
     */
    List<Conversation> findByUserIdOrderByLastUpdateTimeDesc(String userId);
    
    /**
     * 根据用户ID和角色ID查询对话
     */
    List<Conversation> findByUserIdAndRoleIdOrderByLastUpdateTimeDesc(String userId, String roleId);
    
    /**
     * 根据状态查询对话
     */
    List<Conversation> findByStatus(String status);
}
    