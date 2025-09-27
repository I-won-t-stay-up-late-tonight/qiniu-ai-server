package com.qiniuai.chat.audiochat.controller;

import com.qiniuai.chat.audiochat.entity.TtsRequest;
import com.qiniuai.chat.audiochat.service.QwenTtsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实时语音合成:ServerCommit 模式
 * 文档：https://help.aliyun.com/zh/model-studio/interactive-process-of-qwen-tts-realtime-synthesis?spm=a2c4g.11186623.help-menu-2400256.d_2_5_4.46741836Yv4Wzb&scm=20140722.H_2963385._.OR_help-T_cn~zh-V_1
 */
@RestController
@RequestMapping("/api/tts")
public class QwenTtsController {

    // 注入服务层实例（依赖抽象，不依赖具体实现）
    private final QwenTtsService quentTtsService;

    public QwenTtsController(QwenTtsService quentTtsService) {
        this.quentTtsService = quentTtsService;
    }

    @PostMapping(value = "/synthesize-and-download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> synthesizeAndDownload(@RequestBody TtsRequest request) {
        ResponseEntity<byte[]> responseEntity = quentTtsService.synthesizeAndDownload(request);
        return responseEntity;
    }
}
