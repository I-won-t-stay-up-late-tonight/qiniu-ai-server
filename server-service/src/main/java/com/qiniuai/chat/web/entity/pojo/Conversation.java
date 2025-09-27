package com.qiniuai.chat.web.entity.pojo;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @ClassName Conversation
 * @Description TODO
 * @Author IFundo
 * @Date 16:07 2025/9/23
 * @Version 1.0
 */

@Data
@Table(name = "conversations")
public class Conversation {

    private Long id;

    private String userId;

    private String conversationName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
