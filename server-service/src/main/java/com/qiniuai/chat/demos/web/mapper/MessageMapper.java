package com.qiniuai.chat.demos.web.mapper;
import com.qiniuai.chat.demos.web.entity.pojo.DbMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
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
            "ORDER BY send_time ASC")
    List<DbMessage> selectByConversationId(Long conversationId);


    /*
     * @dbMsg 消息实体
     */
    @Insert("INSERT INTO messages (conversation_id, role, content, send_time) " +
            "VALUES (#{conversationId}, #{role}, #{content}, #{sendTime})")
    int insertMessage(DbMessage dbMsg);
}
