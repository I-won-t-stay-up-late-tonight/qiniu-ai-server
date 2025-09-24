package com.qiniuai.chat.demos.web.service.impl;

import com.qiniuai.chat.demos.web.entity.pojo.Role;
import com.qiniuai.chat.demos.web.mapper.ConversationMapper;
import com.qiniuai.chat.demos.web.mapper.ConversationRoleRelationMapper;
import com.qiniuai.chat.demos.web.mapper.RoleMapper;
import com.qiniuai.chat.demos.web.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * @ClassName audioServiceImpl
 * @Description TODO
 * @Author IFundo
 * @Date 00:06 2025/9/23
 * @Version 1.0
 */

@Service
public class conversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationRoleRelationMapper conversationRoleRelationMapper;

    @Autowired
    private RoleMapper roleMapper;

    private static final Long DEFAULT_ROLE_ID = 1L;

    @Override
    public String createConversation(Long userId, String conversationName) {
        Long conversationId = 0L;
        if (userId == null) {
            throw new IllegalArgumentException("用户ID（userId）不能为空");
        }

        try {
            int affectedRows = conversationMapper.insertConversation(userId, conversationName);
            if (affectedRows != 1){
                throw new IllegalArgumentException("插入失败");
            }
        }catch (DuplicateKeyException e){
            return "会话名称已存在";
        }
        return "创建成功";
    }

    /*
     * @Date 16:24 2025/9/24
     * @Description //TODO  全局异常处理器，捕获异常
     * @Author IFundo
     *
     */

    @Override
    public String createConversationAndRole(Long userId, String conversationName, Long roleId) {

        // 1. 校验参数（避免无效输入）
        if (userId == null) {
            throw new IllegalArgumentException("用户ID（userId）不能为空");
        }
        Role role = roleMapper.selectById(roleId);

        if (roleId == null || role == null) {
            roleId = roleMapper.selectById(DEFAULT_ROLE_ID).getId();
        }

        // 2. 创建会话并获取自增ID
        int conversationRows = conversationMapper.insertConversation(
                userId,
                conversationName
        );
        if (conversationRows != 1) {
            throw new RuntimeException("创建会话失败，未生成会话ID");
        }

        long conversationId = conversationMapper.selectConversationIdByUserIdAndName(userId, conversationName);

        // 3. 绑定会话与角色（插入关联关系）
        int relationRows = conversationRoleRelationMapper.insertConversationRoleRelation(
                conversationId,
                roleId
        );
        if (relationRows != 1) {
            throw new RuntimeException("会话与角色绑定失败");
        }

        return "创建成功";
    }

}
