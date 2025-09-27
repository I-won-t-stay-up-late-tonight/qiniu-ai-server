package com.qiniuai.chat.audiochat.controller;
import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.audiochat.service.ASRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
/**
 * 语音识别控制器
 * 处理音频文件上传和实时识别请求
 */
@RestController
@RequestMapping("/api/asr")
public class ASRController {
    private static final Logger logger = LoggerFactory.getLogger(ASRController.class);
    private final ASRService asrService;

    @Autowired
    public ASRController(ASRService asrService) {
        this.asrService = asrService;
    }

    /**
     * 接收前端上传的录音文件，返回识别结果
     */
    @PostMapping("/recognize")
    public ApiResult<Map<String, Object>> recognizeAudio(
            @RequestParam("audioFile") MultipartFile audioFile) {

        CompletableFuture<Map<String, Object>> exceptionally = asrService.recognizeFromMultipartFile(audioFile)
                .thenApply(recognizedText -> buildResponse(true, "识别成功",
                        Map.of("text", recognizedText, "filename", audioFile.getOriginalFilename()),
                        HttpStatus.OK))
                .exceptionally(ex -> {
                    logger.error("识别失败", ex);
                    return buildResponse(false, "识别失败：" + ex.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
                });
        return ApiResult.success(exceptionally.join());
    }

    /**
     * 统一响应格式（便于前端解析）
     */
    private Map<String, Object> buildResponse(boolean success, String message, Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", status.value());
        return response;
    }
}
