package com.qiniuai.chat.audiochat.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import com.qiniuai.chat.audiochat.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * 阿里云语音识别服务，使用paraformer-realtime-8k-v2模型
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunAsrService {

    private final HttpClient httpClient;
    private final AudioService audioService;

    // 阿里云配置
    @Value("${third-party.aliyun.access-key-id}")
    private String accessKeyId;
    @Value("${third-party.aliyun.access-key-secret}")
    private String accessKeySecret;
    @Value("${third-party.aliyun.region-id}")
    private String regionId;
    @Value("${third-party.aliyun.asr.app-key}")
    private String appKey;
    @Value("${third-party.aliyun.asr.url}")
    private String asrUrl;
    @Value("${third-party.aliyun.asr.model}")
    private String model;
    @Value("${third-party.aliyun.asr.format}")
    private String format;
    @Value("${third-party.aliyun.asr.sample-rate}")
    private String sampleRate;

    /**
     * 处理ASR任务，将语音转换为文本
     */
    @Async("asrExecutor")
    public CompletableFuture<String> processAsrTask(Task task) {
        return CompletableFuture.supplyAsync(() -> {
            String conversationId = task.getConversationId();
            String userId = task.getUserId();
            
            try {
                log.info("开始处理ASR任务，任务ID: {}, 对话ID: {}", task.getId(), conversationId);
                
                // 从任务数据中获取音频文件路径
                Map<String, Object> data = task.getData();
                String audioFilePath = (String) data.get("audioFilePath");
                
                if (audioFilePath == null || audioFilePath.isEmpty()) {
                    throw new RuntimeException("音频文件路径为空");
                }
                
                // 读取音频文件并转为Base64
                byte[] audioBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(audioFilePath));
                String audioBase64 = Base64.encode(audioBytes);
                
                // 构建请求参数
                Map<String, String> params = buildAsrParams(audioBase64);
                
                // 生成签名
                String signature = generateSignature(params);
                params.put("Signature", signature);
                
                // 构建HTTP请求
                HttpPost httpPost = new HttpPost(asrUrl);
                String requestBody = com.alibaba.fastjson2.JSON.toJSONString(params);
                httpPost.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
                
                // 执行请求
                return httpClient.execute(httpPost, response -> {
                    int statusCode = response.getCode();
                    if (statusCode == 200 && response.getEntity() != null) {
                        String responseBody = new String(response.getEntity().getContent().readAllBytes(), 
                                StandardCharsets.UTF_8);
                        log.info("ASR识别成功，对话ID: {}, 响应: {}", conversationId, responseBody);
                        
                        // 解析响应结果
                        Map<String, Object> resultMap = com.alibaba.fastjson2.JSON.parseObject(responseBody, Map.class);
                        if ("0".equals(resultMap.get("code").toString())) {
                            Map<String, Object> result = (Map<String, Object>) resultMap.get("result");
                            return result.get("text").toString(); // 返回识别文本
                        } else {
                            String errorMsg = resultMap.get("message").toString();
                            log.error("ASR识别失败，对话ID: {}, 错误信息: {}", conversationId, errorMsg);
                            throw new RuntimeException("ASR识别失败: " + errorMsg);
                        }
                    } else {
                        log.error("ASR调用失败，对话ID: {}, 状态码: {}", conversationId, statusCode);
                        throw new RuntimeException("ASR调用失败，状态码: " + statusCode);
                    }
                });
            } catch (Exception e) {
                log.error("ASR任务处理异常，任务ID: {}, 对话ID: {}", task.getId(), conversationId, e);
                throw new RuntimeException("ASR任务处理异常", e);
            }
        });
    }

    /**
     * 构建ASR请求参数
     */
    private Map<String, String> buildAsrParams(String audioBase64) {
        Map<String, String> params = new TreeMap<>();
        
        // 公共参数
        params.put("AccessKeyId", accessKeyId);
        params.put("Action", "RecognizeSpeech");
        params.put("Format", "json");
        params.put("RegionId", regionId);
        params.put("SignatureMethod", "HMAC-SHA1");
        params.put("SignatureNonce", String.valueOf(System.currentTimeMillis()));
        params.put("SignatureVersion", "1.0");
        params.put("Timestamp", DateUtil.format(DateUtil.date(), "yyyy-MM-dd'T'HH:mm:ss'Z'"));
        params.put("Version", "2019-02-28");
        
        // 业务参数
        params.put("AppKey", appKey);
        params.put("AudioData", audioBase64);
        params.put("Format", format);
        params.put("Model", model);
        params.put("SampleRate", sampleRate);
        params.put("EnablePunctuation", "true");
        params.put("EnableInverseTextNormalization", "true");
        
        return params;
    }

    /**
     * 生成阿里云API签名
     */
    private String generateSignature(Map<String, String> params) throws Exception {
        // 构造待签名字符串
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("Signature".equals(key) || value == null || value.isEmpty()) {
                continue;
            }
            paramStr.append(URLEncoder.encode(key, StandardCharsets.UTF_8.name()))
                    .append("=")
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                    .append("&");
        }
        
        if (paramStr.length() > 0) {
            paramStr.deleteCharAt(paramStr.length() - 1);
        }
        
        // 构造最终待签名字符串
        String method = "POST";
        String urlPath = "/stream/v1/asr";
        String encodedPath = URLEncoder.encode(urlPath, StandardCharsets.UTF_8.name())
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        String encodedParamStr = URLEncoder.encode(paramStr.toString(), StandardCharsets.UTF_8.name())
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        
        String signStr = method + "&" + encodedPath + "&" + encodedParamStr;
        
        // 计算HMAC-SHA1签名
        String signKey = accessKeySecret + "&";
        byte[] signatureBytes = SecureUtil.hmacSha1(signKey.getBytes())
                .digest(signStr.getBytes(StandardCharsets.UTF_8));
        
        // Base64编码
        return Base64.encode(signatureBytes);
    }
}
    