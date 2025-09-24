package com.qiniuai.chat.demos.web.mapper;

import com.qiniuai.chat.demos.web.entity.pojo.Role;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 插入会话-角色关联关系
     * @param conversationId 会话ID
     * @param roleId 角色ID
     * @return 受影响行数（1=成功）
     */
    @Insert("INSERT INTO conversation_role_relation (conversation_id, role_id, create_time) " +
            "VALUES (#{conversationId}, #{roleId}, CURRENT_TIMESTAMP)")
    int insertConversationRoleRelation(
            @Param("conversationId") Long conversationId,
            @Param("roleId") Long roleId
    );
}
