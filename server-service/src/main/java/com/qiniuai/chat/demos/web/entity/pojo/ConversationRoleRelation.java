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
@Entity
@Table(name = "conversation_role_relation") // 关联数据库表名
public class ConversationRoleRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}

