package com.qiniuai.chat.web.controller;


import com.qiniuai.chat.web.dto.XunfeiTtsRequest;
import com.qiniuai.chat.web.service.IXunfeiTtsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tts")
public class XunfeiTtsController {

    private final IXunfeiTtsService ttsService;

    // 构造函数注入服务
    public XunfeiTtsController(IXunfeiTtsService ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * 文字转语音接口（WebAPI方式）
     * 请求体示例：{"text":"测试文本","voiceName":"xiaoyan"}
     */
    @PostMapping("/synthesize-webapi")
    public ResponseEntity<byte[]> synthesize(@RequestBody XunfeiTtsRequest request) {
        try {
            byte[] audioData = ttsService.textToSpeech(request);

            // 设置响应头（PCM格式）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "speech.pcm");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage().getBytes());
        }
    }
}
