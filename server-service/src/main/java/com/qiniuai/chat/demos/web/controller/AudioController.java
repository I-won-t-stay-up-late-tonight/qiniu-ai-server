package com.qiniuai.chat.demos.web.controller;

import com.hnit.server.dto.ApiResult;
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

    /*
     * @Date 09:58 2025/9/24
     * @Description //TODO
     * @Author IFundo
     * @ audio 上传的音频文件
     */
    @PostMapping("/audio2text")
    public ApiResult<String> audioToText(
            @Validated @RequestParam("audio") MultipartFile audio) {

        if (audio.isEmpty()) {
            return ApiResult.fail("No audio file provided");
        }
        String res = audioService.audio2text(audio);
        return res != null ? ApiResult.success(res) : ApiResult.fail(res);

    }
    /*
     * @Date 09:58 2025/9/24
     * @Description //TODO
     * @Author IFundo
     * @content 用户发送的内容
     */

    @PostMapping("/text2audio")
    public ApiResult<String> textToAudio(
            @Validated @RequestParam("content") String content) {

        if (content.isEmpty()) {
            return ApiResult.fail("No content provided");
        }

        String res = audioService.text2audio(content);
        return res != null ? ApiResult.success(res) : ApiResult.fail(res);

    }
    /*
     * @Date 09:36 2025/9/24
     * @Description //TODO
     * @Author IFundo
     * @content 用户发送的内容
     * @id 会话ID
     */

    @PostMapping("/chat")
    public ApiResult<String> chat(
            @Validated @RequestParam("content") String content, @Validated @RequestParam("id") long id
    ) {

        if (content.isEmpty()) {
            return ApiResult.fail("No chat provided");
        }

        String res = audioService.chat(content, id);
        return res != null ? ApiResult.success(res) : ApiResult.fail(res);
    }
    /*
     * @Date 09:58 2025/9/24
     * @Description //TODO
     * @Author IFundo
     * @audio 上传的音频文件
     * @id 会话ID
     */

    @PostMapping("/audioChat")
    public ApiResult<String> audioChat(
            @Validated @RequestParam("audio") MultipartFile audio, @Validated @RequestParam("id") long id
    ) {

        if (audio.isEmpty()) {
            return ApiResult.fail("No audio provided");
        }

        String res = audioService.audioChat(audio, id);
        return res != null ? ApiResult.success(res) : ApiResult.fail(res);

    }
}
