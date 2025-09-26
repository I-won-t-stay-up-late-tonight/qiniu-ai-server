package com.qiniuai.chat.audiochat.entity;

import lombok.Data;

/**
 * 语音识别结果DTO
 * 用于封装识别结果并返回给前端
 */
@Data
public class ASRResult {
    // 任务唯一标识
    private String taskId;

    // 阿里云请求ID，用于追踪请求
    private String requestId;

    // 识别结果类型
    private ResultType resultType;

    // 识别文本内容
    private String text;
}

