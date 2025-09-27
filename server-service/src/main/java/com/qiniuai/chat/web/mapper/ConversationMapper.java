package com.qiniuai.chat.web.mapper;

import com.qiniuai.chat.web.entity.pojo.Conversation;
import com.qiniuai.chat.web.entity.pojo.DbMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
     * @param conversation 会话
     * @return 受影响行数（1=成功）
     */
    @Insert("INSERT INTO conversations (user_id, conversation_name, create_time) " +
            "VALUES (#{userId}, #{conversationName}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertConversation(Conversation conversation);


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

    /**
     * 根据用户ID查询该用户的所有会话记录
     * @param userId 用户ID
     * @return 会话记录列表
     */
    @Select("SELECT " +
            "id, " +                      // 会话ID
            "user_id AS userId, " +       // 用户ID，映射到实体类的userId属性
            "conversation_name AS conversationName, " +  // 会话名称
            "create_time AS createTime, " +  // 创建时间
            "update_time AS updateTime " +  // 更新时间
            "FROM conversations " +
            "WHERE user_id = #{userId} " +  // 按用户ID筛选
            "ORDER BY update_time DESC")    // 按更新时间倒序排列，最新的会话在前
    List<Conversation> searchConversationByUserId(@Param("userId") long userId);


    /*
     * @Date 22:01 2025/9/26
     * @Description 查找符合要求的聊天信息
     *
     */

    @Select("SELECT " +
            "m.id, " +
            "m.conversation_id AS conversationId, " + // 字段映射为实体类的驼峰属性
            "m.role, " +
            "m.content, " +
            "m.send_time AS sendTime, " +
            "m.url " +
            "FROM messages m " +
            "WHERE m.conversation_id = #{conversationId} " +
            "ORDER BY m.send_time DESC LIMIT 30 ") // 按消息ID升序排列（也可按时间排序，需表中有时间字段）
    List<DbMessage> searchMessageHistory(
            @Param("conversationId") long conversationId
    );
}

