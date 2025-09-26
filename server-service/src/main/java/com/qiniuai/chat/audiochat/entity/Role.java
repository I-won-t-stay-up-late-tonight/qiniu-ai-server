package com.qiniuai.chat.audiochat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 角色实体，存储在MongoDB中
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "roles")
public class Role {

    @Id
    private String id;
    
    // 角色名称
    private String name;
    
    // 角色描述
    private String description;
    
    // 角色类别（历史人物、文学角色、科学家等）
    private String category;
    
    // 角色头像URL
    private String avatarUrl;
    
    // 角色开场白
    private String welcomeMessage;
    
    // LLM提示词，定义角色行为和知识范围
    private String systemPrompt;
    
    // 角色技能列表
    private List<String> skills;
    
    // 角色技能详情
    private Map<String, String> skillDetails;
    
    // 科大讯飞音色ID
    private String voiceId;
    
    // 语音参数（语速、音调等）
    private Map<String, Float> voiceParams;
    
    // 角色状态（ENABLED, DISABLED）
    private String status;
    
    // 创建时间
    private LocalDateTime createTime;
    
    // 更新时间
    private LocalDateTime updateTime;
    
    // 热门程度（用于排序）
    private int popularity;
}
    