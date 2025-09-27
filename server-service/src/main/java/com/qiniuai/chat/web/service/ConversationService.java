package com.qiniuai.chat.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;

import com.qiniuai.chat.web.dto.ConversationAndRoleDto;
import com.qiniuai.chat.web.entity.pojo.Conversation;
import com.qiniuai.chat.web.entity.pojo.DbMessage;

import java.util.List;

/**
 * @ClassName audioService
 * @Description TODO
 * @Author IFundo
 * @Date 00:05 2025/9/23
 * @Version 1.0
 */


public interface ConversationService {

    Long createConversation(String userId, String conversationService);

    Long createConversationAndRole(ConversationAndRoleDto conversationAndRoleDto);

    List<Conversation> searchConversationByUserId(long userId);

    List<DbMessage> searchHistoryMessage(long conversationId);
}
