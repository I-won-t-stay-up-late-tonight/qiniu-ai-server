package com.qiniuai.chat.demos.web.mapper;

import com.qiniuai.chat.demos.web.entity.pojo.ConversationRoleRelation;
import org.apache.ibatis.annotations.Param;

/**
 * @ClassName ConversationRoleRelationMapper
 * @Description TODO
 * @Author IFundo
 * @Date 19:19 2025/9/23
 * @Version 1.0
 */
public interface ConversationRoleRelationMapper {

    int insertOrUpdate(@Param("relation") ConversationRoleRelation relation);

    Integer selectRoleIdByConversationId(@Param("conversationId") Long conversationId);
}
