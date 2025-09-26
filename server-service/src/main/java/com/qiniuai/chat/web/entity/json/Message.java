package com.qiniuai.chat.web.entity.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName Message
 * @Description TODO
 * @Author IFundo
 * @Date 12:05 2025/9/23
 * @Version 1.0
 */

@Data
public class Message {
    @JsonProperty("annotations")
    private List<Annotation> annotations;

    @JsonProperty("content")
    private List<Content> content;

    @JsonProperty("role")
    private String role;
}

