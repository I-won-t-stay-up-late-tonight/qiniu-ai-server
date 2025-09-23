package com.qiniuai.chat.demos.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @ClassName Annotation
 * @Description TODO
 * @Author IFundo
 * @Date 12:05 2025/9/23
 * @Version 1.0
 */
@Data
public class Annotation {
    @JsonProperty("language")
    private String language;

    @JsonProperty("type")
    private String type;
}
