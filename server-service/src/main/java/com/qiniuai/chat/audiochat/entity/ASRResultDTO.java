package com.qiniuai.chat.audiochat.entity;

/**
 * 语音识别结果DTO，用于封装服务层返回给控制器的数据
 */
public class ASRResultDTO {
    private String fullText;       // 提取的完整文本
    private String originalJson;   // 原始识别结果JSON

    public ASRResultDTO(String fullText, String originalJson) {
        this.fullText = fullText;
        this.originalJson = originalJson;
    }

    // Getter方法
    public String getFullText() {
        return fullText;
    }

    public String getOriginalJson() {
        return originalJson;
    }
}
