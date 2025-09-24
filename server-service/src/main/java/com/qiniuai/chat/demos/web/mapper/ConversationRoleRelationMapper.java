package com.qiniuai.chat.demos.web.mapper;

import com.qiniuai.chat.demos.web.entity.pojo.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ConversationRoleRelationMapper
 * @Author IFundo
 * @Date 19:19 2025/9/23
 * @Version 1.0
 */

@Mapper
public interface ConversationRoleRelationMapper {

    /**
     * 直接关联conversation_role_relation和roles表，查询会话绑定的角色信息
     * @param conversationId 会话ID
     * @return 角色完整信息（无关联时返回null)
     */
    Role selectByConversationId(@Param("conversationId") Long conversationId);
}
