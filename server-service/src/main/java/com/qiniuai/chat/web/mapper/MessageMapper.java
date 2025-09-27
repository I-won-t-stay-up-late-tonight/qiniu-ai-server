package com.qiniuai.chat.web.mapper;
import com.qiniuai.chat.web.entity.pojo.DbMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @ClassName MessageMapper
 * @Description TODO
 * @Author IFundo
 * @Date 16:14 2025/9/23
 * @Version 1.0
 */
@Mapper
public interface MessageMapper {

    /**
     * 根据会话ID查询消息列表（按发送时间升序）
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @Select("SELECT id, conversation_id, role, content, send_time " +
            "FROM messages " +
            "WHERE conversation_id = #{conversationId} " +
            "ORDER BY send_time ASC LIMIT 30")
    List<DbMessage> selectByConversationId(Long conversationId);


    /*
     * @dbMsg 消息实体
     */
    @Insert("INSERT INTO messages(conversation_id, role, content, send_time, url) " +
            "VALUES(#{conversationId}, #{role}, #{content}, #{sendTime}, #{url})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMessage(DbMessage dbMsg);

    /*
     * @dbMessages 消息实体列表
     */
    void batchInsertMessage(List<DbMessage> dbMessages);

    // 新增方法：更新消息的音频URL
    void updateAudioUrl(@Param("id") Long id, @Param("url") String url);
}
