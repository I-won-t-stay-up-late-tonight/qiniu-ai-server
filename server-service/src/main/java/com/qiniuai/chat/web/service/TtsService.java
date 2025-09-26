package com.qiniuai.chat.web.service;


import com.qiniuai.chat.web.dto.BaiduTtsRequest;
import com.qiniuai.chat.web.dto.BaiduTtsResponse;

import java.io.InputStream;

/**
 * 文本转语音服务接口
 */
public interface TtsService {

    /**
     * 将文本转换为语音，返回输入流
     * @param request 转换请求参数
     * @return 音频输入流
     */
    InputStream textToSpeech(BaiduTtsRequest request);

    /**
     * 将文本转换为语音并上传到七牛云
     * @param request 转换请求参数
     * @return 包含音频URL等信息的响应对象
     */
    BaiduTtsResponse textToSpeechAndUpload(BaiduTtsRequest request);

    /**
     * 保存音频文件到本地
     * @param inputStream 音频输入流
     * @param fileName 文件名(不含扩展名)
     * @param format 音频格式
     * @return 保存路径
     */
    String saveAudioFile(InputStream inputStream, String fileName, String format);

    /**
     * 获取音频格式扩展名
     * @param aue 百度TTS格式代码
     * @return 扩展名
     */
    String getFileExtension(int aue);
}
