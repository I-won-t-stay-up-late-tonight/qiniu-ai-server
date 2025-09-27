package com.qiniuai.chat.web.entity.pojo;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @ClassName Message
 * @Description TODO
 * @Author IFundo
 * @Date 16:09 2025/9/23
 * @Version 1.0
 */

@Data
@Table(name = "messages")
public class DbMessage {

    private Long id;

    private Long conversationId;

    private String role;

    private String content;

    private LocalDateTime sendTime;

}

