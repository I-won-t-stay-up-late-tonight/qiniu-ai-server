package com.qiniuai.chat.demos.web.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 文本转语音响应结果
 */
@Data
@Builder
public class BaiduTtsResponse {
    /**
     * 音频文件在七牛云的URL
     */
    private String audioUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小(字节)
     */
    private long fileSize;

    /**
     * 音频格式
     */
    private String format;

    /**
     * 处理时间(毫秒)
     */
    private long processTime;
}
