package com.qiniuai.chat.demos.web.service.impl;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniuai.chat.demos.web.config.QiniuKodoConfig;
import com.qiniuai.chat.demos.web.dto.BaiduTtsRequest;
import com.qiniuai.chat.demos.web.dto.BaiduTtsResponse;
import com.qiniuai.chat.demos.web.service.TtsService;
import com.qiniuai.chat.demos.web.util.TtsUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * 百度TTS服务实现类
 */
@Slf4j
@Service
public class BaiduTtsServiceImpl implements TtsService {

    private final AipSpeech aipSpeech;
    private final UploadManager uploadManager;
    private final Auth qiniuAuth;
    private final QiniuKodoConfig qiniuConfig;

    // 本地保存路径
    private static final String LOCAL_SAVE_PATH = "audio-files/";

    @Autowired
    public BaiduTtsServiceImpl(AipSpeech aipSpeech, UploadManager uploadManager,
                               Auth qiniuAuth, QiniuKodoConfig qiniuConfig) {
        this.aipSpeech = aipSpeech;
        this.uploadManager = uploadManager;
        this.qiniuAuth = qiniuAuth;
        this.qiniuConfig = qiniuConfig;
        
        // 创建本地保存目录
        createLocalDirectory();
    }

    /**
     * 创建本地保存目录
     */
    private void createLocalDirectory() {
        File directory = new File(LOCAL_SAVE_PATH);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("本地音频保存目录创建成功: {}", LOCAL_SAVE_PATH);
            } else {
                log.warn("本地音频保存目录创建失败: {}", LOCAL_SAVE_PATH);
            }
        }
    }

    @Override
    public InputStream textToSpeech(BaiduTtsRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 调用百度TTS API
            TtsResponse response = aipSpeech.synthesis(
                    request.getText(), 
                    "zh", 
                    1, 
                    getParamMap(request)
            );
            
            // 检查响应
            byte[] data = response.getData();
            JSONObject res = response.getResult();
            
            if (data != null) {
                log.info("文本转语音成功，耗时: {}ms, 文本长度: {}字符", 
                        System.currentTimeMillis() - startTime, 
                        request.getText().length());
                return new ByteArrayInputStream(data);
            } else {
                log.error("文本转语音失败: {}", res.toString());
                throw new RuntimeException("文本转语音失败: " + res.optString("err_msg", "未知错误"));
            }
        } catch (Exception e) {
            log.error("文本转语音发生异常", e);
            throw new RuntimeException("文本转语音发生异常: " + e.getMessage());
        }
    }

    @Override
    public BaiduTtsResponse textToSpeechAndUpload(BaiduTtsRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 转换文本为语音
            InputStream audioStream = textToSpeech(request);
            
            // 2. 准备上传七牛云的信息
            String format = getFileExtension(request.getAue());
            String fileName = generateFileName(request.getFileName(), format);
            
            // 3. 上传到七牛云
            String upToken = qiniuAuth.uploadToken(qiniuConfig.getBucketName());
            Response response = uploadManager.put(
                    TtsUtils.readStream(audioStream),
                    fileName, 
                    upToken
            );
            
            if (!response.isOK()) {
                log.error("七牛云上传失败: {}", response.bodyString());
                throw new RuntimeException("音频上传到七牛云失败");
            }
            
            // 4. 构建返回结果
            long fileSize = TtsUtils.readStream(audioStream).length;
            String audioUrl = qiniuConfig.getDomain() + "/" + fileName;
            
            log.info("文本转语音并上传成功，总耗时: {}ms, 音频URL: {}", 
                    System.currentTimeMillis() - startTime, 
                    audioUrl);
            
            return BaiduTtsResponse.builder()
                    .audioUrl(audioUrl)
                    .fileName(fileName)
                    .fileSize(fileSize)
                    .format(format)
                    .processTime(System.currentTimeMillis() - startTime)
                    .build();
            
        } catch (Exception e) {
            log.error("文本转语音并上传发生异常", e);
            throw new RuntimeException("文本转语音并上传发生异常: " + e.getMessage());
        }
    }

    @Override
    public String saveAudioFile(InputStream inputStream, String fileName, String format) {
        try {
            // 确保文件名不为空
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "audio_" + System.currentTimeMillis();
            }
            
            // 拼接完整文件名
            String fullFileName = fileName + "." + format;
            Path savePath = Paths.get(LOCAL_SAVE_PATH, fullFileName);
            
            // 保存文件
            Files.copy(inputStream, savePath);
            log.info("音频文件保存到本地: {}", savePath.toAbsolutePath());
            
            return savePath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("保存音频文件失败", e);
            throw new RuntimeException("保存音频文件失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileExtension(int aue) {
        switch (aue) {
            case 3: return "mp3";
            case 4: return "pcm";
            case 5: return "pcm";
            case 6: return "wav";
            default: return "mp3";
        }
    }

    /**
     * 生成百度TTS参数Map
     */
    private HashMap<String, Object> getParamMap(BaiduTtsRequest request) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("per", request.getPer());
        params.put("spd", request.getSpd());
        params.put("pit", request.getPit());
        params.put("vol", request.getVol());
        params.put("aue", request.getAue());
        return params;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFileName, String format) {
        // 如果提供了文件名，则使用提供的文件名
        if (originalFileName != null && !originalFileName.trim().isEmpty()) {
            return originalFileName + "." + format;
        }
        
        // 否则生成一个唯一文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        
        return "tts/" + dateStr + "/" + uuid + "." + format;
    }
}
