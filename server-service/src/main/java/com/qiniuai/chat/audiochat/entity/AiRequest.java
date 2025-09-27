package com.qiniuai.chat.audiochat.entity;


import lombok.Data;

/**
 * 前端请求实体（用户输入内容）
 */
@Data
public class AiRequest {
    /**
     * 用户输入的文本内容
     */
    private String userContent;
}