package com.qiniuai.chat.web.service.impl;

import com.qiniu.util.StringUtils;
import com.qiniuai.chat.web.dto.ConversationAndRoleDto;
import com.qiniuai.chat.web.entity.pojo.Conversation;
import com.qiniuai.chat.web.entity.pojo.DbMessage;
import com.qiniuai.chat.web.entity.pojo.Role;
import com.qiniuai.chat.web.mapper.ConversationMapper;
import com.qiniuai.chat.web.mapper.ConversationRoleRelationMapper;
import com.qiniuai.chat.web.mapper.RoleMapper;
import com.qiniuai.chat.web.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * @ClassName audioServiceImpl
 * @Description TODO
 * @Author IFundo
 * @Date 00:06 2025/9/23
 * @Version 1.0
 */

@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationRoleRelationMapper conversationRoleRelationMapper;

    @Autowired
    private RoleMapper roleMapper;

    private static final Long DEFAULT_ROLE_ID = 1L;

    @Override
    public Long createConversation(String userId, String conversationName) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID（userId）不能为空");
        }
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setConversationName(conversationName);
        conversationMapper.insertConversation(conversation);
        Long id = conversation.getId();
        System.out.println("id = " + id);
        return id;
    }

    /*
     * @Date 16:24 2025/9/24
     * @Description //TODO  全局异常处理器，捕获异常
     * @Author IFundo
     *
     */

    @Override
    public Long createConversationAndRole(ConversationAndRoleDto conversationAndRoleDto) {
        if (StringUtils.isNullOrEmpty(conversationAndRoleDto.getUserId())) {
            throw new IllegalArgumentException("用户ID（userId）不能为空");
        }
        Role role = roleMapper.selectById(conversationAndRoleDto.getRoleId());

        if (conversationAndRoleDto.getRoleId() == null || role == null) {
            conversationAndRoleDto.setRoleId(roleMapper.selectById(DEFAULT_ROLE_ID).getId());
        }
        // 2. 创建会话
        Conversation conversation = new Conversation();
        conversation.setUserId(conversationAndRoleDto.getUserId());
        conversation.setConversationName(conversationAndRoleDto.getConversationName());
        conversationMapper.insertConversation(conversation);

        if (conversation.getId() == null) {
            throw new RuntimeException("创建会话失败，未生成会话ID");
        }

        long conversationId = conversation.getId();

        // 3. 绑定会话与角色（插入关联关系）
        int relationRows = conversationRoleRelationMapper.insertConversationRoleRelation(
                conversationId,
                conversationAndRoleDto.getRoleId()
        );
        if (relationRows != 1) {
            throw new RuntimeException("会话与角色绑定失败");
        }

        return Long.valueOf(conversationId);
    }

    @Override
    public List<Conversation> searchConversationByUserId(String userId) {
        List<Conversation> conversations = conversationMapper.searchConversationByUserId(userId);
        return conversations;
    }


    @Override
    public List<DbMessage> searchHistoryMessage(long conversationId) {
        List<DbMessage> historyMessage = conversationMapper.searchMessageHistory(conversationId);
        return historyMessage;
    }

}
