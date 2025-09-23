package com.qiniuai.chat.demos.web.entity.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName Output
 * @Description TODO
 * @Author IFundo
 * @Date 12:02 2025/9/23
 * @Version 1.0
 */
@Data
public class Output {
    @JsonProperty("choices")
    private List<Choice> choices;
}
