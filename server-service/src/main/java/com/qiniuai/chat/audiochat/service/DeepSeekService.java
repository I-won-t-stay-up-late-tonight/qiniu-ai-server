package com.qiniuai.chat.audiochat.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek 模型服务层：处理核心业务逻辑
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeepSeekService {
    private final Generation generation;

    @Value("${ali.audio.tts.api-key}")
    private String apiKey;

    private final String modelName = "deepseek-v3.1";
    private final String systemPrompt = "你是一个专业的助手，请基于用户输入内容，用简洁明了的语言回答，回答长度不超过500字";

    /**
     * 核心方法：调用 DeepSeek 模型，返回完整响应
     * @param userContent 用户输入文本
     * @return 模型生成的响应文本
     * @throws Exception 调用异常（向上抛出，由Controller统一处理）
     */
    public String callDeepSeekModel(String userContent, String promptWord) throws Exception {
        // 1. 业务参数校验（服务层自己校验，不依赖Controller）
        if (userContent == null || userContent.trim().isEmpty()) {
            log.warn("用户输入内容为空");
            throw new IllegalArgumentException("请输入有效内容");
        }

        // 2. 构建对话消息（系统提示词 + 用户输入）
        List<Message> messages = buildChatMessages(userContent, promptWord);
        log.debug("构建对话消息：{}", messages);

        // 3. 构建模型请求参数（非流式配置）
        GenerationParam param = buildGenerationParam(messages);

        // 4. 调用模型（同步非流式，等待完整结果）
        log.info("开始调用 DeepSeek 模型：{}，用户输入：{}", modelName, userContent);
        GenerationResult result = generation.call(param);

        // 5. 解析模型响应结果
        return parseModelResult(result, userContent);
    }

    /**
     * 辅助方法：构建对话消息列表（系统提示词 + 用户输入）
     */
    private List<Message> buildChatMessages(String userContent,String promptWord) {
        List<Message> messages = new ArrayList<>();
        // 系统提示词（固定角色：SYSTEM）
        messages.add(Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(promptWord)
                .build());
        // 用户输入（角色：USER）
        messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userContent.trim())
                .build());
        return messages;
    }

    /**
     * 辅助方法：构建模型请求参数（非流式）
     */
    private GenerationParam buildGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                .apiKey(apiKey)          // 阿里云API Key
                .model(modelName)        // 模型名称（deepseek-v3.1）
                .messages(messages)      // 对话消息列表
                .resultFormat(GenerationParam.ResultFormat.MESSAGE) // 结果格式为MESSAGE
                .build();                // 非流式：无需额外配置（默认非流式）
    }

    /**
     * 辅助方法：解析模型返回结果，提取响应文本
     */
    private String parseModelResult(GenerationResult result, String userContent) {
        // 校验结果合法性
        if (result == null || result.getOutput() == null || result.getOutput().getChoices().isEmpty()) {
            log.error("模型响应为空，用户输入：{}，请求参数：{}", userContent, result);
            throw new RuntimeException("模型未返回有效结果");
        }

        // 提取第一个结果的文本内容（模型可能返回多个候选，此处取第一个）
        String modelResponse = result.getOutput().getChoices().get(0).getMessage().getContent();
        log.info("模型调用成功，用户输入：{}，响应内容：{}", userContent, modelResponse);
        return modelResponse;
    }
}