package com.qiniuai.chat.demos.web.mapper;

import org.apache.ibatis.annotations.*;

/**
 * @ClassName ConversationMapper
 * @Description TODO
 * @Author IFundo
 * @Date 16:18 2025/9/23
 * @Version 1.0
 */
@Mapper
public interface ConversationMapper {


    /**
     * 插入会话并返回自增ID
     * @param userId 用户ID
     * @param conversationName 会话名称
     * @return 受影响行数（1=成功）
     */
    @Insert("INSERT INTO conversations (user_id, conversation_name) " +
            "VALUES (#{userId}, #{conversationName})")
    int insertConversation(
            @Param("userId") Long userId,
            @Param("conversationName") String conversationName
    );


    /**
     * 根据 userId 和 conversationName 查询会话ID（即表的 id 列）
     * @param userId 用户ID（对应表的 user_id 字段）
     * @param conversationName 会话名称（对应表的 conversation_name 字段）
     * @return 匹配的会话ID（若查询不到则返回 null，因用包装类 Long）；
     *         因表有唯一索引 uk_user_conversation_name，最多返回1条结果
     */
    @Select("SELECT id " + // 数据库表的主键列名是 id，对应业务的 conversationId
            "FROM conversations " + // 表名，确保与数据库一致
            "WHERE user_id = #{userId} " + // 条件1：匹配用户ID
            "AND conversation_name = #{conversationName}") // 条件2：匹配会话名称
    Long selectConversationIdByUserIdAndName(
            @Param("userId") Long userId, // 明确参数名，避免多参数解析混乱
            @Param("conversationName") String conversationName // 明确参数名
    );
}

