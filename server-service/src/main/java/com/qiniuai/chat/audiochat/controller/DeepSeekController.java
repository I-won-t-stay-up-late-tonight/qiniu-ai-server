package com.qiniuai.chat.audiochat.controller;


import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.audiochat.entity.AiRequest;
import com.qiniuai.chat.audiochat.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 阿里云 DashScope DeepSeek 模型控制器（非流式）
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor // 构造器注入（推荐）
@Slf4j
public class DeepSeekController {

    // 注入服务层实例（依赖抽象，不依赖具体实现）
    private final DeepSeekService deepSeekService;
    private final String systemPrompt = "你是一个专业的助手，请基于用户输入内容，用简洁明了的语言回答，回答长度不超过500字";

    /**
     * 非流式对话接口：接收用户输入，返回模型完整响应
     * @param request 前端请求（包含用户输入文本）
     * @return 统一响应模型（成功/失败结果）
     */
    @PostMapping("/chat")
    public ApiResult<String> chatWithModel(@RequestBody AiRequest request) {
        try {

            // 1. 调用服务层处理业务（Controller不做业务逻辑，只转发）
            String modelResponse = deepSeekService.callDeepSeekModel(request.getUserContent(),systemPrompt);

            // 2. 返回成功响应
            return ApiResult.success(modelResponse);

        } catch (IllegalArgumentException e) {
            // 2. 处理参数错误（如用户输入为空）
            log.warn("请求参数错误：{}", e.getMessage());
            return ApiResult.fail(400, e.getMessage());

        } catch (Exception e) {
            // 3. 处理其他业务异常（如模型调用失败）
            log.error("模型对话接口异常", e);
            return ApiResult.fail(500, "调用模型失败：" + e.getMessage());
        }
    }
}