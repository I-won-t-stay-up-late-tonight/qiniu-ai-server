package com.qiniuai.chat.demos.web.service.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.qiniuai.chat.demos.web.entity.AudioResponse;
import com.qiniuai.chat.demos.web.service.AudioService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * @ClassName audioServiceImpl
 * @Description TODO
 * @Author IFundo
 * @Date 00:06 2025/9/23
 * @Version 1.0
 */

@Service
public class audioServiceImpl implements AudioService {

    @Override
    public String audio2text(MultipartFile audio) throws ApiException, NoApiKeyException, UploadFileException, IOException {

        byte[] audioBytes = audio.getBytes();
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

        // 构建包含Base64音频频的内容（格式：data:audio/[格式];base64,[编码内容]）
        String audioContentType = audio.getContentType(); // 获取音频MIME类型（如audio/mpeg）
        String audioDataUrl = "data:" + audioContentType + ";base64," + base64Audio;

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("audio", audioDataUrl) // 使用转换后的音频数据
                ))
                .build();

        MultiModalMessage sysMessage = MultiModalMessage.builder().role(Role.SYSTEM.getValue())
                // 此处用于配置定制化识别的Context
                .content(Arrays.asList(Collections.singletonMap("text", "")))
                .build();

        Map<String, Object> asrOptions = new HashMap<>();
        asrOptions.put("enable_lid", true);
        asrOptions.put("enable_itn", false);
        // asrOptions.put("language", "zh"); // 可选，若已知音频的语种，可通过该参数指定待识别语种，以提升识别准确率

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-95513ded49764ba6a533d427797b6f20")
                .model("qwen3-asr-flash")
                .message(userMessage)
                .message(sysMessage)
                .parameter("asr_options", asrOptions)
                .build();

        MultiModalConversationResult result = conv.call(param);
        String resJson = JsonUtils.toJson(result);
        String test = jsonTextExtractor(resJson);
        return test != null ? test : "解析失败";

    }

    @Override
    public String text2audio(String content) throws ApiException, NoApiKeyException, UploadFileException {
        String MODEL = "qwen3-tts-flash";
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey("sk-95513ded49764ba6a533d427797b6f20")
                .model(MODEL)
                .text(content)
                .voice(AudioParameters.Voice.CHERRY)
                .languageType("Chinese") // 建议与文本语种一致，以获得正确的发音和自然的语调。
                .build();
        MultiModalConversationResult result = conv.call(param);
        String audioUrl = result.getOutput().getAudio().getUrl();
        System.out.println("audioUrl = " + audioUrl);

        // 下载音频文件到本地
        try (InputStream in = new URL(audioUrl).openStream();
             FileOutputStream out = new FileOutputStream("downloaded_audio.wav")) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("\n音频文件已下载到本地: downloaded_audio.wav");
        } catch (Exception e) {
            System.out.println("\n下载音频文件时出错: " + e.getMessage());
        }
        return audioUrl;
    }

    private String jsonTextExtractor(String resJson){
        try {
            // 创建ObjectMapper实例
            ObjectMapper objectMapper = new ObjectMapper();
            // 解析JSON字符串为JsonNode
            JsonNode rootNode = objectMapper.readTree(resJson);

            // 按照JSON路径提取text内容
            String text = rootNode
                    .path("output")
                    .path("choices")
                    .get(0)  // 获取数组第一个元素
                    .path("message")
                    .path("content")
                    .get(0)  // 获取数组第一个元素
                    .path("text")
                    .asText();  // 转换为字符串

           return text;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
