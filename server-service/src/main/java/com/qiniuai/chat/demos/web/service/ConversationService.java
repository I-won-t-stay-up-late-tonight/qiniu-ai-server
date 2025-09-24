package com.qiniuai.chat.demos.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;

/**
 * @ClassName audioService
 * @Description TODO
 * @Author IFundo
 * @Date 00:05 2025/9/23
 * @Version 1.0
 */


public interface ConversationService {

    String createConversation(Long userId, String conversationService);

    String createConversationAndRole(Long userId, String conversationName, Long roleId);
}
