package com.qiniuai.chat.audiochat.service;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.qiniuai.chat.audiochat.entity.ASRResultDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Service
public class ASRService {
    private static final Logger logger = LoggerFactory.getLogger(ASRService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MODEL = "paraformer-realtime-v2";
    private static final int SAMPLE_RATE = 16000; // 模型要求的固定采样率

    @Value("${ali.audio.tts.api-key}")
    private String apiKey;

    /**
     * 处理音频识别核心逻辑
     * @param file 音频文件
     * @return 识别结果DTO对象
     * @throws Exception 处理过程中的异常
     */
    public ASRResultDTO recognizeAudio(MultipartFile file) throws Exception {
        // 验证文件格式
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isSupportedFormat(fileExtension)) {
            throw new IllegalArgumentException("不支持的音频格式，仅支持wav、mp3、pcm");
        }

        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = createTempFile(file, fileExtension);

            // 构建识别参数
            RecognitionParam param = RecognitionParam.builder()
                    .model(MODEL)
                    .apiKey(apiKey)
                    .format(fileExtension)
                    .sampleRate(SAMPLE_RATE)
                    .parameters(Collections.singletonMap(
                            "language_hints", new String[]{"zh", "en"}
                    ))
                    .build();

            // 执行识别
            Recognition recognizer = new Recognition();
            String recognitionResult = recognizer.call(param, tempFile);
            logger.debug("原始识别结果: {}", recognitionResult);

            // 解析识别结果
            String fullText = parseRecognitionResult(recognitionResult);

            // 返回封装后的结果
            return new ASRResultDTO(fullText, recognitionResult);

        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    logger.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 解析识别结果JSON，提取文本内容
     */
    private String parseRecognitionResult(String jsonResult) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonResult);
        StringBuilder fullText = new StringBuilder();

        if (rootNode.has("sentences") && rootNode.get("sentences").isArray()) {
            JsonNode sentencesNode = rootNode.get("sentences");
            for (JsonNode sentence : sentencesNode) {
                if (sentence.has("text")) {
                    fullText.append(sentence.get("text").asText()).append(" ");
                }
            }
        }

        return fullText.toString().trim();
    }

    /**
     * 创建临时文件
     */
    private File createTempFile(MultipartFile file, String extension) throws IOException {
        // 使用UUID生成唯一文件名，避免冲突
        String tempFileName = "asr_" + UUID.randomUUID() + "." + extension;
        File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        // 设置临时文件JVM退出时自动删除
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "wav"; // 默认wav格式
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查是否为支持的音频格式
     */
    private boolean isSupportedFormat(String extension) {
        return "wav".equals(extension) || "mp3".equals(extension) || "pcm".equals(extension);
    }
}
