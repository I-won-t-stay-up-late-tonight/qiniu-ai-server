package com.qiniuai.chat.audiochat.entity;

/**
 * 识别结果类型枚举
 */
public enum ResultType {
    // 中间结果（非最终）
    INTERMEDIATE,

    // 最终结果（句子结束）
    FINAL,

    // 识别完成
    COMPLETE,

    // 识别错误
    ERROR
}