package com.qiniuai.chat.audiochat.service;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionResult;
import com.alibaba.dashscope.common.ResultCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 语音识别服务
 * 封装阿里云ASR SDK的调用逻辑
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Service
public class ASRService{
    private static final Logger logger = LoggerFactory.getLogger(ASRService.class);
    // 阿里ASR配置（16KHz采样率的WAV文件，每次发送100ms数据：16000Hz * 16bit * 1ch / 8bit = 3200字节）
    private static final int CHUNK_SIZE = 3200; // 核心：100ms音频数据的字节数
    private static final String MODEL = "paraformer-realtime-v2"; // 阿里实时识别模型
    private static final String API_KEY = "sk-95513ded49764ba6a533d427797b6f20"; // 替换为你的API Key（建议配置在环境变量中）


    public CompletableFuture<String> recognizeFromMultipartFile(MultipartFile audioFile) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        StringBuilder fullResult = new StringBuilder(); // 拼接最终识别结果
        CountDownLatch latch = new CountDownLatch(1); // 等待识别完成

        try (InputStream inputStream = audioFile.getInputStream()) { // 直接从MultipartFile获取输入流（无临时文件）
            // 1. 构建阿里ASR参数
            RecognitionParam param = RecognitionParam.builder()
                    .apiKey(API_KEY)
                    .model(MODEL)
                    .format("wav") // 与前端录音格式一致
                    .sampleRate(16000) // 阿里ASR推荐16KHz
                    .parameter("language_hints", new String[]{"zh", "en"}) // 支持中英双语
                    .build();

            // 2. 创建ASR识别客户端
            Recognition recognizer = new Recognition();
            String threadName = Thread.currentThread().getName();

            // 3. 配置识别结果回调
            ResultCallback<RecognitionResult> callback = new ResultCallback<>() {
                @Override
                public void onEvent(RecognitionResult message) {
                    // 中间结果（可选打印，前端无需实时展示）
                    if (!message.isSentenceEnd()) {
                        logger.debug("[{}] 中间结果：{}", threadName, message.getSentence().getText());
                        return;
                    }
                    // 最终句子结果（拼接）
                    String sentence = message.getSentence().getText();
                    fullResult.append(sentence).append(" ");
                    logger.info("[{}] 最终句子结果：{}", threadName, sentence);
                }

                @Override
                public void onComplete() {
                    logger.info("[{}] 识别完成，总结果：{}", threadName, fullResult.toString().trim());
                    latch.countDown(); // 通知主线程识别完成
                    resultFuture.complete(fullResult.toString().trim()); // 返回最终结果
                }

                @Override
                public void onError(Exception e) {
                    logger.error("[{}] 识别异常", threadName, e);
                    resultFuture.completeExceptionally(e); // 传递异常
                }
            };

            // 4. 启动ASR连接
            recognizer.call(param, callback);
            logger.info("开始处理录音文件：{}（大小：{}KB）", audioFile.getOriginalFilename(), audioFile.getSize() / 1024);

            // 5. 内存流分块读取音频数据，发送给ASR（核心：无临时文件）
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                ByteBuffer audioBuffer = ByteBuffer.wrap(buffer, 0, bytesRead); // 读取实际长度（避免最后一块补0）
                recognizer.sendAudioFrame(audioBuffer); // 发送音频块
                logger.debug("发送音频块：{}字节", bytesRead);
                Thread.sleep(100); // 模拟实时流（100ms发送一次，与CHUNK_SIZE匹配）
            }

            // 6. 发送结束信号，等待识别完成
            recognizer.stop();
            latch.await(); // 等待回调的onComplete执行

            // 7. 打印识别 metrics（可选）
            logger.info("[Metric] 请求ID：{}，首包延迟：{}ms，末包延迟：{}ms",
                    recognizer.getLastRequestId(),
                    recognizer.getFirstPackageDelay(),
                    recognizer.getLastPackageDelay());

        } catch (Exception e) {
            logger.error("处理录音文件异常", e);
            resultFuture.completeExceptionally(e);
        }

        return resultFuture;
    }
}