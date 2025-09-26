package com.qiniuai.chat.audiochat.controller;

import com.alibaba.dashscope.audio.qwen_tts_realtime.QwenTtsRealtime;
import com.alibaba.dashscope.audio.qwen_tts_realtime.QwenTtsRealtimeAudioFormat;
import com.alibaba.dashscope.audio.qwen_tts_realtime.QwenTtsRealtimeCallback;
import com.alibaba.dashscope.audio.qwen_tts_realtime.QwenTtsRealtimeConfig;
import com.alibaba.dashscope.audio.qwen_tts_realtime.QwenTtsRealtimeParam;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.gson.JsonObject;
import com.qiniuai.chat.audiochat.entity.TtsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 实时语音合成:ServerCommit 模式
 * 文档：https://help.aliyun.com/zh/model-studio/interactive-process-of-qwen-tts-realtime-synthesis?spm=a2c4g.11186623.help-menu-2400256.d_2_5_4.46741836Yv4Wzb&scm=20140722.H_2963385._.OR_help-T_cn~zh-V_1
 */
@RestController
@RequestMapping("/api/tts")
public class QwenTtsController {

    @Value("${ali.audio.tts.api-key}")
    private String apiKey;

    @PostMapping(value = "/synthesize-and-download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> synthesizeAndDownload(@RequestBody TtsRequest request) {
        try {
            // 创建音频收集器
            AudioCollector audioCollector = new AudioCollector(request.getSampleRate());

            // 配置TTS参数
            QwenTtsRealtimeParam param = QwenTtsRealtimeParam.builder()
                    .model("qwen-tts-realtime")
                    .apikey(apiKey)
                    .build();

            CountDownLatch completeLatch = new CountDownLatch(1);
            AtomicReference<QwenTtsRealtime> qwenTtsRef = new AtomicReference<>(null);

            // 创建TTS实例
            QwenTtsRealtime qwenTtsRealtime = new QwenTtsRealtime(param, new QwenTtsRealtimeCallback() {
                @Override
                public void onOpen() {
                    // 连接建立时的处理
                }

                @Override
                public void onEvent(JsonObject message) {
                    String type = message.get("type").getAsString();
                    switch(type) {
                        case "response.audio.delta":
                            String recvAudioB64 = message.get("delta").getAsString();
                            // 收集音频数据
                            audioCollector.collect(recvAudioB64);
                            break;
                        case "session.finished":
                            // 会话结束时的处理
                            completeLatch.countDown();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onClose(int code, String reason) {
                    // 连接关闭时的处理
                }
            });

            qwenTtsRef.set(qwenTtsRealtime);

            try {
                qwenTtsRealtime.connect();
            } catch (NoApiKeyException e) {
                throw new RuntimeException("API密钥未配置", e);
            }

            // 配置语音参数
            QwenTtsRealtimeConfig config = QwenTtsRealtimeConfig.builder()
                    .voice(request.getVoice())
                    .languageType(request.getLanguageType())
                    .responseFormat(QwenTtsRealtimeAudioFormat.PCM_24000HZ_MONO_16BIT)
                    .mode("server_commit")
                    .build();

            qwenTtsRealtime.updateSession(config);

            // 发送文本进行合成
            for (String text : request.getTextList()) {
                qwenTtsRealtime.appendText(text);
                Thread.sleep(100);
            }

            qwenTtsRealtime.finish();
            completeLatch.await();

            // 生成WAV文件
            byte[] wavData = audioCollector.generateWavFile();


            // 设置响应头，让浏览器下载文件
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"synthesized-speech.wav\"");

            return new ResponseEntity<>(wavData, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 音频收集器，负责收集并转换音频数据
     */
    private static class AudioCollector {
        private final int sampleRate;
        private final Queue<byte[]> audioChunks = new ConcurrentLinkedQueue<>();
        private final AtomicBoolean isCollecting = new AtomicBoolean(true);

        public AudioCollector(int sampleRate) {
            this.sampleRate = sampleRate;
        }

        public void collect(String base64Audio) {
            if (isCollecting.get() && base64Audio != null && !base64Audio.isEmpty()) {
                byte[] audioData = Base64.getDecoder().decode(base64Audio);
                audioChunks.add(audioData);
            }
        }


        /**
         * 将收集的PCM数据转换为WAV格式
         */
        public byte[] generateWavFile() throws IOException {
            isCollecting.set(false);

            // 计算总数据大小并合并所有音频块
            int totalSize = 0;
            for (byte[] chunk : audioChunks) {
                totalSize += chunk.length;
            }
            byte[] pcmData = new byte[totalSize];
            int position = 0;
            for (byte[] chunk : audioChunks) {
                System.arraycopy(chunk, 0, pcmData, position, chunk.length);
                position += chunk.length;
            }

            // 创建PCM音频格式
            AudioFormat format = new AudioFormat(
                    sampleRate,    // 采样率
                    16,            // 位深度
                    1,             // 声道数 (单声道)
                    true,          // 有符号
                    false          // 小端字节序
            );

            // 关键修复：将PCM数据包装为AudioInputStream
            try (ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
                 AudioInputStream audioInputStream = new AudioInputStream(
                         bais,
                         format,
                         pcmData.length / format.getFrameSize() // 计算帧数
                 );
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, baos);
                return baos.toByteArray();
            }
        }
    }
}
