package com.qiniuai.chat.audiochat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 语音合成请求参数实体类
 * 支持的音色:
 *  Cherry: 阳光积极、亲切自然小姐姐。
 *  Ethan: 标准普通话，带部分北方口音。阳光、温暖、活力、朝气。
 *  Nofish: 不会翘舌音的设计师。
 *  Jennifer: 品牌级、电影质感般美语女声。
 *  Sunny: 甜到你心里的川妹子。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TtsRequest {
    private String[] textList;
    private String voice = "Chelsie"; // 默认语音
    private String languageType = "Chinese"; // 默认语言
    private int sampleRate = 24000; // 默认采样率
}
