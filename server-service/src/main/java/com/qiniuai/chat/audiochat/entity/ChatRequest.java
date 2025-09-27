package com.qiniuai.chat.audiochat.entity;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private String model = "deepseek-reasoner";
    private List<ChatMessage> messages;
    private boolean stream = true;
}