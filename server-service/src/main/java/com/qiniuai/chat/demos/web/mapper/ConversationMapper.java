package com.qiniuai.chat.demos.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
     * 检查会话是否存在
     * @param id 会话ID
     * @return 存在返回1，否则返回0
     */
    @Select("SELECT COUNT(1) FROM conversations WHERE id = #{id}")
    int existsById(Long id);
}

