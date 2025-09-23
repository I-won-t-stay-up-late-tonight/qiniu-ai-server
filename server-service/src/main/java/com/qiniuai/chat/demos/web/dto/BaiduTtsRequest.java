package com.qiniuai.chat.demos.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本转语音请求参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaiduTtsRequest {

    /**
     * 需要转换的文本内容
     */
    @NotBlank(message = "文本内容不能为空")
    private String text;

    /**
     * 发音人选择, 0为女声，1为男声，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为0
     */
    private Integer per = 0;

    /**
     * 语速，取值0-15，默认为5中语速
     */
    private Integer spd = 5;

    /**
     * 音调，取值0-15，默认为5中语调
     */
    private Integer pit = 5;

    /**
     * 音量，取值0-9，默认为5中音量
     */
    private Integer vol = 5;

    /**
     * 音频格式，3为mp3格式(默认)；4为pcm-16k；5为pcm-8k；6为wav（内容同pcm-16k）
     */
    private Integer aue = 3;

    /**
     * 保存到七牛云的文件名(不包含扩展名)，为空则自动生成
     */
    private String fileName;
}
