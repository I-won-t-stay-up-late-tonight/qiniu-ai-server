package com.qiniuai.chat.demos.web.entity.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @ClassName Content
 * @Description TODO
 * @Author IFundo
 * @Date 12:05 2025/9/23
 * @Version 1.0
 */
@Data
public class Content {
    @JsonProperty("text")
    private String text;
}

