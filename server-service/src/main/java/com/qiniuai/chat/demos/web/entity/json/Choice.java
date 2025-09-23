package com.qiniuai.chat.demos.web.entity.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @ClassName Choice
 * @Description TODO
 * @Author IFundo
 * @Date 12:04 2025/9/23
 * @Version 1.0
 */

@Data
public class Choice {
    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonProperty("message")
    private Message message;
}
