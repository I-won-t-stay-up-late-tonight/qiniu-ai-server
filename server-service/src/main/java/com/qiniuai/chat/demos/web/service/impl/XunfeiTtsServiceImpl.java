package com.qiniuai.chat.demos.web.service.impl;

import com.qiniuai.chat.demos.web.dto.XunfeiTtsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.qiniuai.chat.demos.web.service.IXunfeiTtsService;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;


@Service
@Slf4j
public class XunfeiTtsServiceImpl implements IXunfeiTtsService {

    @Value("${xunfei.tts.app-id}")
    private String appId;
    @Value("${xunfei.tts.api-key}")
    private String apiKey;
    @Value("${xunfei.tts.api-secret}")
    private String apiSecret;

    // 讯飞TTS WebAPI的WebSocket地址
    private static final String WEB_SOCKET_URL = "wss://spark-api.xf-yun.com/v4.0/chat";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 文字转语音（WebAPI方式）
     * @return 音频字节数组（PCM格式）
     */
    public byte[] textToSpeech(XunfeiTtsRequest request) throws Exception {
        String text = request.getText();
        String voiceName = request.getRole();
        // 1. 生成WebSocket连接地址（带签名）
        String url = generateWebSocketUrl();
        if (url == null) {
            throw new RuntimeException("生成WebSocket连接地址失败");
        }

        // 2. 初始化WebSocket客户端
        CountDownLatch latch = new CountDownLatch(1);
        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
        final String[] errorMsg = {null};

        WebSocketClient client = new WebSocketClient(new URI(url)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                try {
                    // 连接成功后发送合成请求
                    String requestBody = buildTtsRequest(text, voiceName);
                    this.send(requestBody);
                } catch (Exception e) {
                    errorMsg[0] = "发送请求失败：" + e.getMessage();
                    latch.countDown();
                }
            }

            @Override
            public void onMessage(String message) {
                try {
                    // 解析服务端返回的JSON
                    JsonNode root = objectMapper.readTree(message);
                    int code = root.get("code").asInt();

                    if (code != 0) {
                        // 错误处理
                        errorMsg[0] = "合成错误：" + code + "，" + root.get("message").asText();
                        latch.countDown();
                        return;
                    }

                    // 提取音频数据（Base64编码）
                    String audioBase64 = root.get("data").get("audio").asText();
                    if (audioBase64 != null && !audioBase64.isEmpty()) {
                        byte[] audioData = Base64.getDecoder().decode(audioBase64);
                        audioStream.write(audioData);
                    }

                    // 判断是否为最后一段数据
                    if (root.get("data").get("status").asInt() == 2) {
                        audioStream.flush();
                        latch.countDown(); // 合成完成
                    }
                } catch (Exception e) {
                    errorMsg[0] = "处理响应失败：" + e.getMessage();
                    latch.countDown();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                if (errorMsg[0] == null && code != 1000) {
                    errorMsg[0] = "连接意外关闭：" + reason;
                }
                if (latch.getCount() > 0) {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Exception ex) {
                errorMsg[0] = "WebSocket错误：" + ex.getMessage();
                if (latch.getCount() > 0) {
                    latch.countDown();
                }
            }
        };

        // 3. 建立连接并等待合成完成
        client.connect();
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        client.close();

        // 4. 处理结果
        if (!completed) {
            throw new RuntimeException("合成超时");
        }
        if (errorMsg[0] != null) {
            throw new RuntimeException(errorMsg[0]);
        }

        return audioStream.toByteArray();
    }

    /**
     * 生成带签名的WebSocket连接地址
     */
    private String generateWebSocketUrl() {
        try {
            // 生成RFC1123格式的时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            String date = sdf.format(new Date());

            // 拼接签名串
            String signatureOrigin = "host: tts-api.xfyun.cn\n" +
                    "date: " + date + "\n" +
                    "GET /v2/tts HTTP/1.1";

            // 使用API Secret进行HMAC-SHA256加密
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(signatureBytes);

            // 拼接Authorization
            String authorization = "api_key=\"" + apiKey + "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";
            String authorizationBase64 = Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));

            // 生成最终的WebSocket地址
            return WEB_SOCKET_URL + "?authorization=" + authorizationBase64 + "&date=" + date + "&host=tts-api.xfyun.cn";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 构建TTS请求JSON
     */
    private String buildTtsRequest(String text, String voiceName) throws Exception {
        Map<String, Object> request = new HashMap<>();
        // 基础参数
        Map<String, Object> common = new HashMap<>();
        common.put("app_id", appId);
        request.put("common", common);

        // 业务参数
        Map<String, Object> business = new HashMap<>();
        business.put("aue", "raw"); // 音频格式：raw（PCM）、lame（MP3）
        business.put("auf", "audio/L16;rate=16000"); // 采样率：16000Hz
        business.put("voice_name", voiceName); // 语音角色
        business.put("speed", 50); // 语速（0-100）
        business.put("volume", 50); // 音量（0-100）
        business.put("pitch", 50); // 音调（0-100）
        request.put("business", business);

        // 文本参数
        Map<String, Object> data = new HashMap<>();
        data.put("text", Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8))); // 文本Base64编码
        request.put("data", data);

        return objectMapper.writeValueAsString(request);
    }
}

