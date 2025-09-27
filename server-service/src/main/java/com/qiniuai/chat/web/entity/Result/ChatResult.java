package com.qiniuai.chat.web.entity.Result;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName ChatResult
 * @Description TODO
 * @Author IFundo
 * @Date 15:02 2025/9/27
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class ChatResult {
    private String content;
    private Long userMessageId;
    private Long assistantMessageId;
}