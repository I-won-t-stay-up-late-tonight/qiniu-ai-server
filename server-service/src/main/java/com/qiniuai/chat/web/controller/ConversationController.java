package com.qiniuai.chat.web.controller;

import com.hnit.server.dto.ApiResult;
import com.qiniu.util.StringUtils;
import com.qiniuai.chat.web.dto.ConversationAndRoleDto;
import com.qiniuai.chat.web.entity.pojo.Conversation;
import com.qiniuai.chat.web.entity.pojo.DbMessage;
import com.qiniuai.chat.web.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @ClassName ConversationController
 * @Description TODO
 * @Author IFundo
 * @Date 14:52 2025/9/24
 * @Version 1.0
 */

@RestController
@RequestMapping("/api/v1/")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;
    /*
     * @Date 21:09 2025/9/26
     * 创建会话
     */
    @PostMapping("/createConversation")
    public ApiResult<Long> createConversation(
            @Validated @RequestParam("userId") String userId, @Validated @RequestParam(value = "conversationName", required = false) String conversationName) {
        if (conversationName == null || conversationName.trim().isEmpty()) {
            conversationName = "默认会话";
        }
        Long id = conversationService.createConversation(userId, conversationName);
        return id != null ? ApiResult.success(id) : ApiResult.fail("创建失败");

    }
    /*
     * @Date 21:09 2025/9/26
     * 创建会话
     */
    @PostMapping("/createConversationAndRole")
    public ApiResult<Long> createConversationAndRole(@RequestBody ConversationAndRoleDto conversationAndRoleDto) {
        if (StringUtils.isNullOrEmpty(conversationAndRoleDto.getConversationName())) {
            conversationAndRoleDto.setConversationName("默认会话");
        }
        Long res = conversationService.createConversationAndRole(conversationAndRoleDto);
        return res != null ? ApiResult.success(res) : ApiResult.fail("创建失败");

    }

    /*
     * @Date 21:09 2025/9/26
     * 搜索会话
     */
    @PostMapping("/searchConversation")
    public ApiResult<List<Conversation>> searchRole(@Validated long userId) {

        List<Conversation> conversations = conversationService.searchConversationByUserId(userId);
        if (conversations.size() != 0){
            return ApiResult.success(conversations);
        }else {
            return ApiResult.fail(300, "无历史记录");
        }
    }

    /*
     * @Date 21:09 2025/9/26
     * 搜索会话
     */
    @PostMapping("/searchHistoryMessage")
    public ApiResult<List<DbMessage>> searchHistoryMessage(
        @Validated @RequestParam(value = "conversationId") Long conversationId) {
        List<DbMessage> historyMessage = conversationService.searchHistoryMessage(conversationId);
        return ApiResult.success(historyMessage);
    }
}
