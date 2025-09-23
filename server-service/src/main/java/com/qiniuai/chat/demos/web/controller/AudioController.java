package com.qiniuai.chat.demos.web.controller;

import com.qiniuai.chat.demos.web.result.Result;
import com.qiniuai.chat.demos.web.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @Validated @RequestParam("audio") MultipartFile audio) {

        if (audio.isEmpty()) {
            return Result.failed("No audio file provided");
        }
        String res = audioService.audio2text(audio);
        return res != null ? Result.success(res) : Result.failed(res);

    }

    @PostMapping("/text2audio")
    public Result<String> textToAudio(
            @Validated @RequestParam("content") String content) {

        if (content.isEmpty()) {
            return Result.failed("No content provided");
        }

        String res = audioService.text2audio(content);
        return res != null ? Result.success(res) : Result.failed(res);

    }

    @PostMapping("/chat")
    public Result<String> chat(
            @Validated @RequestParam("content") String content, @Validated @RequestParam("id") long id
    ) {

        if (content.isEmpty()) {
            return Result.failed("No chat provided");
        }

        String res = audioService.chat(content, id);
        return res != null ? Result.success(res) : Result.failed(res);
    }

    @PostMapping("/audioChat")
    public Result<String> audioChat(
            @Validated @RequestParam("audio") MultipartFile audio, @Validated @RequestParam("id") long id
    ) {

        if (audio.isEmpty()) {
            return Result.failed("No audio provided");
        }

        String res = audioService.audioChat(audio, id);
        return res != null ? Result.success(res) : Result.failed(res);

    }
}
