package com.qiniuai.chat.demos.rolechat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话实体，存储在MongoDB中
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;
    
    // 用户ID
    private String userId;
    
    // 角色ID
    private String roleId;
    
    // 角色名称
    private String roleName;
    
    // 对话标题
    private String title;
    
    // 消息ID列表
    private List<String> messageIds;
    
    // 对话状态（ACTIVE, PAUSED, ENDED）
    private String status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 最后更新时间
    private LocalDateTime lastUpdateTime;
    
    // 最后一条消息时间
    private LocalDateTime lastMessageTime;
    
    // 对话时长（秒）
    private long duration;
}
    