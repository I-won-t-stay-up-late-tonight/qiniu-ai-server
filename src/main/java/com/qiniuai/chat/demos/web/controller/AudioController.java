package com.qiniuai.chat.demos.web.controller;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.qiniuai.chat.demos.web.result.Result;
import com.qiniuai.chat.demos.web.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @ClassName AudioController
 * @Description TODO
 * @Author IFundo
 * @Date 23:59 2025/9/22
 * @Version 1.0
 */

@RestController
@RequestMapping("/api/v1/")
public class AudioController {

    @Autowired
    private AudioService audioService;

    @PostMapping("/audio2text")
    public Result<String> audioToText(
            @Validated @RequestParam("audio") MultipartFile audio
    ) {

        if (audio.isEmpty()) {
            return Result.failed("No audio file provided");
        }

//        return Result.success("test ok");
        try {
            return Result.success(audioService.audio2text(audio));
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/text2audio")
    public Result<String> textToAudio(
            @Validated @RequestParam("content") String content
    ) {

        if (content.isEmpty()) {
            return Result.failed("No content provided");
        }

//        return Result.success("test ok");

        try {
            return Result.success(audioService.text2audio(content));
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (UploadFileException e) {
            throw new RuntimeException(e);
        }
    }
}
