package com.qiniuai.chat.audiochat.controller;
import com.qiniuai.chat.audiochat.entity.ASRResultDTO;
import com.qiniuai.chat.audiochat.service.ASRService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 语音识别控制器
 * 处理音频文件上传和实时识别请求
 */
@RestController
public class ASRController {
    private final ASRService asrService;

    // 构造器注入Service
    public ASRController(ASRService asrService) {
        this.asrService = asrService;
    }

    /**
     * 语音识别接口
     * @param file 音频文件
     * @return 识别结果及相关信息
     */
    @PostMapping("/api/asr/recognize")
    public ResponseEntity<Map<String, Object>> recognizeAudio2(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = new HashMap<>();

        // 验证文件
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("error", "请上传音频文件");
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        try {
            // 调用Service层处理业务逻辑
            ASRResultDTO recognitionResult = asrService.recognizeAudio(file);

            // 构建成功响应
            result.put("success", true);
            result.put("fullText", recognitionResult.getFullText());
            result.put("originalResult", recognitionResult.getOriginalJson());

            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // 处理参数错误
            result.put("success", false);
            result.put("error", e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // 处理其他异常
            result.put("success", false);
            result.put("error", "识别过程出错: " + e.getMessage());
            return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
