package com.qiniuai.chat.web.entity.json;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @ClassName AudioResponse
 * @Description TODO
 * @Author IFundo
 * @Date 12:02 2025/9/23
 * @Version 1.0
 */
@Data
public class AudioResponse {
    @JsonProperty("output")
    private Output output;

    @JsonProperty("usage")
    private Usage usage;

    @JsonProperty("request_id")
    private String requestId;
}
