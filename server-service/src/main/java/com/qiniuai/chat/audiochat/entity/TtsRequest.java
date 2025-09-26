package com.qiniuai.chat.audiochat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语音合成请求参数实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TtsRequest {
    private String[] textList;
    private String voice = "Chelsie"; // 默认语音
    private String languageType = "Chinese"; // 默认语言
    private int sampleRate = 24000; // 默认采样率
}
