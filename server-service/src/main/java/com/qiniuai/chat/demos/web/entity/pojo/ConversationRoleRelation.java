package com.qiniuai.chat.demos.web.entity.pojo;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @ClassName ConversationRoleRelation
 * @Description TODO
 * @Author IFundo
 * @Date 19:19 2025/9/23
 * @Version 1.0
 */
@Data
@Table(name = "conversation_role_relation") // 关联数据库表名
public class ConversationRoleRelation {

    private Integer id;

    private Long conversationId;

    private Integer roleId;

    private LocalDateTime createTime;
}

