package com.qiniuai.chat.web.entity.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @ClassName Usage
 * @Description TODO
 * @Author IFundo
 * @Date 12:03 2025/9/23
 * @Version 1.0
 */
@Data
public class Usage {
    @JsonProperty("input_tokens_details")
    private TokenDetails inputTokensDetails;

    @JsonProperty("output_tokens_details")
    private TokenDetails outputTokensDetails;

    @JsonProperty("seconds")
    private Integer seconds;
}

