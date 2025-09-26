package com.qiniuai.chat.demos.web.controller;

import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.demos.web.entity.pojo.Conversation;
import com.qiniuai.chat.demos.web.entity.pojo.Role;
import com.qiniuai.chat.demos.web.service.ConversationService;
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

    @PostMapping("/createConversation")
    public ApiResult<String> createConversation(
            @Validated @RequestParam("userId") Long userId, @Validated @RequestParam(value = "conversationName", required = false) String conversationName) {
        if (conversationName == null || conversationName.trim().isEmpty()) {
            conversationName = "默认会话";
        }
        String res = conversationService.createConversation(userId, conversationName);
        return res != null ? ApiResult.success(res) : ApiResult.fail(res);

    }
    @PostMapping("/createConversationAndRole")
    public ApiResult<String> createConversationAndRole(
            @Validated @RequestParam("userId") Long userId, @Validated @RequestParam(value = "conversationName", required = false) String conversationName, @RequestParam(value = "roleId", required = false)Long roleId) {
        if (conversationName == null || conversationName.trim().isEmpty()) {
            conversationName = "默认会话";
        }
        String res = conversationService.createConversationAndRole(userId, conversationName, roleId);
        return res != null ? ApiResult.success(res) : ApiResult.fail(res);

    }

    @GetMapping("/searchConversation")
    public ApiResult<List<Conversation>> searchRole(@Validated long userId) {

        List<Conversation> conversations = conversationService.searchConversationByUserId(userId);
        return ApiResult.success(conversations);
    }
}
