package com.qiniuai.chat.web.controller;

import com.qiniuai.chat.web.dto.XunfeiTtsRequest;
import com.qiniuai.chat.web.service.IXunfeiTtsV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 讯飞TTS v2接口控制器（对外提供HTTP接口）
 */
@RestController
@RequestMapping("/api/tts/v2")
public class XunfeiTtsV2Controller {
    @Autowired
    private IXunfeiTtsV2Service ttsV2Service;

    /**
     * 文字转语音接口
     * @param request 请求体（包含text和可选的voiceName）
     * @return 音频文件（PCM格式，可直接下载）
     */
    @PostMapping("/synthesize")
    public ResponseEntity<byte[]> synthesize(@RequestBody XunfeiTtsRequest request) {
        try {
            // 1. 获取请求参数
            String text = request.getText();
            String voiceName = request.getRole() ;

            // 2. 调用服务合成音频
            byte[] audioData = ttsV2Service.textToSpeech(text, voiceName);

            // 3. 构建响应头（支持浏览器下载）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "tts_speech.pcm"); // 下载文件名

            // 4. 返回音频数据
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(audioData);
        } catch (IllegalArgumentException e) {
            // 参数错误：返回400
            return ResponseEntity.badRequest().body(e.getMessage().getBytes());
        } catch (Exception e) {
            // 系统错误：返回500
            return ResponseEntity.internalServerError().body(("合成失败：" + e.getMessage()).getBytes());
        }
    }
}