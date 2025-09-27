package com.qiniuai.chat.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XunfeiTtsRequest {

    @NotBlank(message = "合成文本不能为空")
    private String text;

    private String role = "xiaoyan";

    @PositiveOrZero(message = "语速不能为负数")
    @Max(value = 100, message = "语速最大为100")
    private int speed = 50;

    @PositiveOrZero(message = "音量不能为负数")
    @Max(value = 100, message = "音量最大为100")
    private int volume = 70;

    private String audioFormat = "pcm";
}
