package com.qiniuai.chat.audiochat.controller;
import com.qiniuai.chat.audiochat.entity.ASRResult;
import com.qiniuai.chat.audiochat.entity.ResultType;
import com.qiniuai.chat.audiochat.service.ASRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
/**
 * 语音识别控制器
 * 处理音频文件上传和实时识别请求
 */
@RestController
@RequestMapping("/api/asr")
public class ASRController {

    private final ASRService asrService;

    @Autowired
    public ASRController(ASRService asrService) {
        this.asrService = asrService;
    }

    /**
     * 处理音频文件上传并进行实时语音识别
     * 使用SSE(Server-Sent Events)实时推送识别结果
     */
    @PostMapping(value = "/recognize", consumes = "multipart/form-data")
    public SseEmitter recognizeAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "languageHints", defaultValue = "zh,en") String[] languageHints,
            @RequestParam(value = "sampleRate", defaultValue = "16000") int sampleRate,
            @RequestParam(value = "format", defaultValue = "wav") String format) {

        // 创建唯一任务ID
        String taskId = UUID.randomUUID().toString();

        // 创建SSE发射器，设置超时时间为5分钟
        SseEmitter emitter = new SseEmitter(300000L);

        try {
            // 启动识别任务
            asrService.startRecognition(
                    taskId,
                    file,
                    format,
                    sampleRate,
                    languageHints,
                    result -> {
                        try {
                            // 向客户端发送识别结果
                            emitter.send(SseEmitter.event()
                                    .name(result.getResultType().toString())
                                    .data(result));

                            // 如果识别完成或出错，关闭连接
                            if (result.getResultType() == ResultType.COMPLETE ||
                                    result.getResultType() == ResultType.ERROR) {
                                emitter.complete();
                            }
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    }
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        // 注册连接关闭时的回调
        emitter.onCompletion(() -> asrService.cleanupTask(taskId));
        emitter.onTimeout(() -> asrService.cleanupTask(taskId));
        emitter.onError((e) -> asrService.cleanupTask(taskId));

        return emitter;
    }

    /**
     * 停止指定的识别任务
     */
    @PostMapping("/stop/{taskId}")
    public ResponseEntity<ASRResult> stopRecognition(@PathVariable String taskId) {
        ASRResult result = asrService.stopRecognition(taskId);
        return new ResponseEntity<>(result,
                result.getResultType() == ResultType.ERROR ?
                        HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.OK);
    }
}
