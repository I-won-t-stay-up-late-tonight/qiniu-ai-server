package com.qiniuai.chat.audiochat.service;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionResult;
import com.alibaba.dashscope.common.ResultCallback;
import jakarta.annotation.PreDestroy;
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

import java.util.concurrent.*;

@Service
public class ASRService {
    private static final Logger logger = LoggerFactory.getLogger(ASRService.class);
    // 调整为50ms的音频块（1600字节），减少单次传输数据量
    private static final int CHUNK_SIZE = 1600; // 16000Hz * 16bit * 1ch / 8 * 0.05s = 1600字节
    private static final String MODEL = "paraformer-realtime-v2";
    private static final String API_KEY = "sk-95513ded49764ba6a533d427797b6f20";

    // 线程池优化：使用缓存线程池处理IO密集操作
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public CompletableFuture<String> recognizeFromMultipartFile(MultipartFile audioFile) {
        // 使用线程池执行，避免阻塞主线程
        return CompletableFuture.supplyAsync(() -> {
            StringBuilder fullResult = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);

            try (InputStream inputStream = audioFile.getInputStream()) {
                // 构建参数时增加超时设置
                RecognitionParam param = RecognitionParam.builder()
                        .apiKey(API_KEY)
                        .model(MODEL)
                        .format("wav")
                        .sampleRate(16000)
                        .parameter("language_hints", new String[]{"zh", "en"})
                        // 增加超时参数，单位秒
                        .parameter("timeout", 30)
                        // 开启快速识别模式（牺牲一点准确率换取速度）
                        .parameter("speed_mode", true)
                        .build();

                Recognition recognizer = new Recognition();
                String threadName = Thread.currentThread().getName();

                // 结果回调保持不变
                ResultCallback<RecognitionResult> callback = new ResultCallback<>() {
                    @Override
                    public void onEvent(RecognitionResult message) {
                        if (message.isSentenceEnd()) {
                            String sentence = message.getSentence().getText();
                            fullResult.append(sentence).append(" ");
                            logger.info("[{}] 最终句子结果：{}", threadName, sentence);
                        }
                    }

                    @Override
                    public void onComplete() {
                        logger.info("[{}] 识别完成，总结果：{}", threadName, fullResult.toString().trim());
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        logger.error("[{}] 识别异常", threadName, e);
                        latch.countDown();
                        throw new RuntimeException(e);
                    }
                };

                // 启动识别
                recognizer.call(param, callback);
                logger.info("开始处理录音文件：{}（大小：{}KB）",
                        audioFile.getOriginalFilename(), audioFile.getSize() / 1024);

                // 优化点1：使用NIO非阻塞读取
                ByteBuffer buffer = ByteBuffer.allocateDirect(CHUNK_SIZE);
                byte[] temp = new byte[CHUNK_SIZE];
                int bytesRead;

                // 优化点2：减少等待时间，与块大小匹配
                long sleepTime = 50; // 50ms，与CHUNK_SIZE对应

                while ((bytesRead = inputStream.read(temp)) != -1) {
                    buffer.clear();
                    buffer.put(temp, 0, bytesRead);
                    buffer.flip();

                    // 优化点3：异步发送音频帧
                    executor.submit(() -> recognizer.sendAudioFrame(buffer));

                    logger.debug("发送音频块：{}字节", bytesRead);
                    Thread.sleep(sleepTime);
                }

                // 发送结束信号
                recognizer.stop();
                // 优化点4：设置等待超时，避免无限阻塞
                if (!latch.await(30, TimeUnit.SECONDS)) {
                    throw new TimeoutException("语音识别超时");
                }

                logger.info("[Metric] 请求ID：{}，首包延迟：{}ms，末包延迟：{}ms",
                        recognizer.getLastRequestId(),
                        recognizer.getFirstPackageDelay(),
                        recognizer.getLastPackageDelay());

                return fullResult.toString().trim();

            } catch (Exception e) {
                logger.error("处理录音文件异常", e);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    // 应用关闭时释放资源
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
