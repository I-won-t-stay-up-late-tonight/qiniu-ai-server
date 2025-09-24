package com.qiniuai.chat.demos.web.service;

/**
 * 讯飞TTS v2接口服务层接口
 */
public interface IXunfeiTtsV2Service {
    /**
     * 文字转语音
     * @param text 待合成文本（最大5000字）
     * @param voiceName 语音角色（如：xiaoyan、en_us_001）
     * @return 音频字节数组（默认PCM格式）
     * @throws Exception 合成异常
     */
    byte[] textToSpeech(String text, String voiceName) throws Exception;

    /**
     * 文字转语音（使用默认参数）
     * @param text 待合成文本
     * @return 音频字节数组
     * @throws Exception 合成异常
     */
    byte[] textToSpeech(String text) throws Exception;
}