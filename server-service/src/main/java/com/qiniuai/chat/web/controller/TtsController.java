package com.qiniuai.chat.web.controller;

import com.hnit.server.dto.ApiResult;
import com.qiniuai.chat.web.dto.BaiduTtsRequest;
import com.qiniuai.chat.web.dto.BaiduTtsResponse;
import com.qiniuai.chat.web.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * 文本转语音
 */
@Slf4j
@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;

    @Autowired
    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    /**
     * 将文本转换为语音并返回音频文件
     */
    @PostMapping("/text-to-audio")
    @Operation(summary = "文本转语音", description = "将文本转换为语音并返回音频文件")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "转换成功",
                    content = @Content(mediaType = "audio/mpeg")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "转换失败")
    })
    public ResponseEntity<byte[]> textToAudio(
            @Valid @RequestBody 
            @Parameter(description = "文本转语音请求参数", required = true)
            BaiduTtsRequest request) {
        try {
            // 获取音频输入流
            InputStream inputStream = ttsService.textToSpeech(request);
            
            // 转换为字节数组
            byte[] audioData = inputStream.readAllBytes();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            String format = ttsService.getFileExtension(request.getAue());
            headers.setContentType(getMediaType(format));
            headers.setContentLength(audioData.length);
            headers.setContentDispositionFormData("attachment", "tts-audio." + format);
            
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("文本转语音接口异常", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 将文本转换为语音并上传到七牛云，返回音频URL
     */
    @PostMapping("/text-to-audio/upload")
    @Operation(summary = "文本转语音并上传到七牛云", description = "将文本转换为语音并上传到七牛云，返回音频URL")
    public ApiResult<BaiduTtsResponse> textToAudioAndUpload(
            @Valid @RequestBody 
            @Parameter(description = "文本转语音请求参数", required = true)
            BaiduTtsRequest request) {
        try {
            BaiduTtsResponse response = ttsService.textToSpeechAndUpload(request);
            return ApiResult.success(response);
        } catch (Exception e) {
            log.error("文本转语音并上传接口异常", e);
            return ApiResult.fail(e.getMessage());
        }
    }

    /**
     * 将文本转换为语音并保存到本地
     */
    @PostMapping("/text-to-audio/save-local")
    @Operation(summary = "测试接口：文本转语音并保存到本地", description = "将文本转换为语音并保存到本地服务器")
    public ApiResult<String> textToAudioAndSaveLocal(
            @Valid @RequestBody 
            @Parameter(description = "文本转语音请求参数", required = true)
            BaiduTtsRequest request) {
        try {
            InputStream inputStream = ttsService.textToSpeech(request);
            String format = ttsService.getFileExtension(request.getAue());
            String savePath = ttsService.saveAudioFile(inputStream, request.getFileName(), format);
            return ApiResult.success("音频文件已保存到本地: " + savePath);
        } catch (Exception e) {
            log.error("文本转语音并保存本地接口异常", e);
            return ApiResult.fail(e.getMessage());
        }
    }

    /**
     * 根据格式获取MediaType
     */
    private MediaType getMediaType(String format) {
        return switch (format.toLowerCase()) {
            case "mp3" -> MediaType.parseMediaType("audio/mpeg");
            case "wav" -> MediaType.parseMediaType("audio/wav");
            case "pcm" -> MediaType.parseMediaType("audio/x-pcm");
            default -> MediaType.parseMediaType("audio/mpeg");
        };
    }
}
