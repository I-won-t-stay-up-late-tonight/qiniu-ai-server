package com.qiniuai.chat.web.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniuai.chat.web.config.XunfeiTtsConfig;
import com.qiniuai.chat.web.service.IXunfeiTtsV2Service;
import com.qiniuai.chat.web.util.XunfeiSignUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 讯飞TTS v2接口服务实现（核心逻辑）
 */
@Service
public class XunfeiTtsV2ServiceImpl implements IXunfeiTtsV2Service {
    // 固定参数（v2接口不可修改）
    private static final Logger log = LoggerFactory.getLogger(XunfeiTtsV2ServiceImpl.class);
    private static final String HOST = "tts-api.xfyun.cn";
    private static final String REQUEST_LINE = "GET /v2/tts HTTP/1.1";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private XunfeiTtsConfig ttsConfig;

    @Override
    public byte[] textToSpeech(String text) throws Exception {
        return textToSpeech(text, ttsConfig.getRoleMapping().get("default"));
    }

    @Override
    public byte[] textToSpeech(String text, String voiceName) throws Exception {
        // 参数校验
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("合成文本不能为空");
        }
        if (text.length() > 5000) {
            throw new IllegalArgumentException("合成文本长度不能超过5000字");
        }
        voiceName = voiceName == null ? "xiaoyan" : voiceName;
        log.info("开始语音合成，文本长度：{}，语音角色：{}", text.length(), voiceName);

        // 生成签名参数
        String date = getRfc1123Date();
        String sign = XunfeiSignUtil.generateSign(ttsConfig.getApiSecret(), HOST, date, REQUEST_LINE);

        // 构建并编码Authorization
        String authHeader = String.format(
                "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                ttsConfig.getApiKey(), sign
        );
        String encodedAuth = URLEncoder.encode(authHeader, StandardCharsets.UTF_8.name());
        String encodedDate = URLEncoder.encode(date, StandardCharsets.UTF_8.name());

        // 构建WebSocket URL
        String fullWssUrl = String.format(
                "%s?authorization=%s&date=%s&host=%s",
                ttsConfig.getHostUrl(), encodedAuth, encodedDate, HOST
        );
        log.debug("WebSocket连接地址：{}", fullWssUrl);

        // 初始化同步工具和数据存储
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<byte[]> audioDataRef = new AtomicReference<>(new byte[0]);
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        AtomicReference<String> fullResponseLog = new AtomicReference<>("");

        // 创建WebSocket客户端
        String finalVoiceName = voiceName;
        WebSocketClient client = new WebSocketClient(new URI(fullWssUrl)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("WebSocket连接成功，开始发送合成请求");
                try {
                    String requestJson = buildTtsRequestJson(text, finalVoiceName);
                    log.debug("发送合成请求：{}", requestJson);
                    this.send(requestJson);
                } catch (Exception e) {
                    log.error("发送请求失败", e);
                    errorRef.set(e);
                    latch.countDown();
                }
            }

            @Override
            public void onMessage(String message) {
                log.debug("收到WebSocket消息：{}", message);
                fullResponseLog.set(fullResponseLog.get() + message + "\n");

                try {
                    JsonNode responseNode = OBJECT_MAPPER.readTree(message);

                    // 处理接口错误
                    if (responseNode.has("code") && responseNode.get("code").asInt() != 0) {
                        String errorMsg = String.format(
                                "接口返回错误：code=%d, message=%s",
                                responseNode.get("code").asInt(),
                                responseNode.get("message").asText()
                        );
                        throw new RuntimeException(errorMsg);
                    }

                    // 处理音频数据
                    if (responseNode.has("data")) {
                        JsonNode dataNode = responseNode.get("data");
                        // 解析音频片段
                        if (dataNode.has("audio") && !dataNode.get("audio").asText().isEmpty()) {
                            byte[] audioChunk = Base64.getDecoder().decode(dataNode.get("audio").asText());
                            audioDataRef.set(mergeAudioData(audioDataRef.get(), audioChunk));
                            log.debug("已接收音频片段，累计长度：{}字节", audioDataRef.get().length);
                        }
                        // 判断是否为最后一块
                        if (dataNode.has("status") && dataNode.get("status").asInt() == 2) {
                            log.info("音频接收完成，总长度：{}字节", audioDataRef.get().length);
                            latch.countDown();
                        }
                    }
                } catch (Exception e) {
                    log.error("处理消息失败", e);
                    errorRef.set(e);
                    latch.countDown();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("WebSocket连接关闭，代码：{}，原因：{}", code, reason);
                if (latch.getCount() > 0) {
                    log.warn("连接提前关闭，可能未接收完整音频");
                    latch.countDown();
                }
            }

            @Override
            public void onError(Exception ex) {
                log.error("WebSocket错误", ex);
                errorRef.set(ex);
                if (latch.getCount() > 0) {
                    latch.countDown();
                }
            }
        };

        try {
            // 执行连接和等待
            client.connect();
            boolean isCompleted = latch.await(5000L, TimeUnit.MILLISECONDS);

            // 超时处理
            if (!isCompleted) {
                String errorMsg = String.format("语音合成超时（%dms）", 5000L);
                log.error(errorMsg + "\n接口响应日志：" + fullResponseLog.get());
                throw new RuntimeException(errorMsg);
            }

            // 异常处理
            Exception error = errorRef.get();
            if (error != null) {
                log.error("合成失败", error);
                log.error("接口响应日志：" + fullResponseLog.get());
                throw new RuntimeException("合成失败：" + error.getMessage(), error);
            }

            // 音频数据校验
            byte[] audioData = audioDataRef.get();
            if (audioData.length == 0) {
                String errorMsg = "未获取到合成音频数据";
                log.error(errorMsg + "\n接口响应日志：" + fullResponseLog.get());
                throw new RuntimeException(errorMsg + "，请检查参数或查看日志");
            }

            return audioData;
        } finally {
            // 确保连接关闭
            if (client.isOpen()) {
                client.close();
            }
        }
    }

    /**
     * 生成RFC1123格式日期
     */
    private String getRfc1123Date() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    /**
     * 构建合成请求JSON
     */
    private String buildTtsRequestJson(String text, String voiceName) {
        // 转义特殊字符，避免JSON格式错误
        String escapedText = text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        return String.format("{\n" +
                        "  \"appid\": \"%s\",\n" +
                        "  \"text\": \"%s\",\n" +
                        "  \"voice\": \"%s\",\n" +
                        "  \"speed\": %d,\n" +
                        "  \"volume\": %d,\n" +
                        "  \"format\": \"%s\"\n" +
                        "}",
                ttsConfig.getAppId(),
                escapedText,
                voiceName,
                1500,
                "xiaoyan",
                "mp3"
        );
    }

    /**
     * 合并音频数据
     */
    private byte[] mergeAudioData(byte[] oldData, byte[] newChunk) {
        byte[] mergedData = new byte[oldData.length + newChunk.length];
        System.arraycopy(oldData, 0, mergedData, 0, oldData.length);
        System.arraycopy(newChunk, 0, mergedData, oldData.length, newChunk.length);
        return mergedData;
    }
}