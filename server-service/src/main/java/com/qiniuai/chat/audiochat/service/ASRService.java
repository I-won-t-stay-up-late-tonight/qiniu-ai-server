package com.qiniuai.chat.audiochat.service;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionResult;
import com.alibaba.dashscope.common.ResultCallback;
import com.qiniuai.chat.audiochat.entity.ASRResult;
import com.qiniuai.chat.audiochat.entity.ResultType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 语音识别服务
 * 封装阿里云ASR SDK的调用逻辑
 */
@Service
public class ASRService {

    // 从配置文件获取API Key
    @Value("${ali.audio.asr.api-key}")
    private String apiKey;

    // 线程池用于处理异步识别任务
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // 存储当前运行的识别任务
    private final Map<String, Recognition> runningTasks = new ConcurrentHashMap<>();

    /**
     * 启动语音识别任务
     */
    public void startRecognition(
            String taskId,
            MultipartFile file,
            String format,
            int sampleRate,
            String[] languageHints,
            Consumer<ASRResult> resultConsumer) {

        // 提交任务到线程池异步执行
        executorService.submit(() -> {
            RecognitionParam param = buildRecognitionParam(format, sampleRate, languageHints);
            Recognition recognizer = new Recognition();
            runningTasks.put(taskId, recognizer);

            try (InputStream inputStream = file.getInputStream()) {
                // 设置识别结果回调
                ResultCallback<RecognitionResult> callback = createResultCallback(
                        taskId, resultConsumer, recognizer);

                // 初始化识别器
                recognizer.call(param, callback);

                // 读取音频文件并发送到识别服务
                sendAudioData(inputStream, recognizer);

                // 通知识别服务音频发送完成
                recognizer.stop();

            } catch (Exception e) {
                // 发送错误结果
                ASRResult errorResult = new ASRResult();
                errorResult.setTaskId(taskId);
                errorResult.setResultType(ResultType.ERROR);
                errorResult.setText("识别过程出错: " + e.getMessage());
                resultConsumer.accept(errorResult);
            }
        });
    }

    /**
     * 构建识别参数
     */
    private RecognitionParam buildRecognitionParam(
            String format, int sampleRate, String[] languageHints) {

        RecognitionParam.RecognitionParamBuilder builder = RecognitionParam.builder()
                .model("paraformer-realtime-v2")
                .format(format)
                .sampleRate(sampleRate)
                .parameter("language_hints", languageHints);

        // 如果配置了API Key则使用配置的，否则使用环境变量
        if (!apiKey.isEmpty()) {
            builder.apiKey(apiKey);
        }

        return builder.build();
    }

    /**
     * 创建识别结果回调
     */
    private ResultCallback<RecognitionResult> createResultCallback(
            String taskId,
            Consumer<ASRResult> resultConsumer,
            Recognition recognizer) {

        String threadName = Thread.currentThread().getName();

        return new ResultCallback<RecognitionResult>() {
            @Override
            public void onEvent(RecognitionResult result) {
                ASRResult asrResult = new ASRResult();
                asrResult.setTaskId(taskId);
                asrResult.setRequestId(recognizer.getLastRequestId());
                asrResult.setText(result.getSentence().getText());

                if (result.isSentenceEnd()) {
                    asrResult.setResultType(ResultType.FINAL);
                } else {
                    asrResult.setResultType(ResultType.INTERMEDIATE);
                }

                resultConsumer.accept(asrResult);
            }

            @Override
            public void onComplete() {
                ASRResult result = new ASRResult();
                result.setTaskId(taskId);
                result.setRequestId(recognizer.getLastRequestId());
                result.setResultType(ResultType.COMPLETE);
                result.setText("识别完成");
                resultConsumer.accept(result);
            }

            @Override
            public void onError(Exception e) {
                ASRResult result = new ASRResult();
                result.setTaskId(taskId);
                result.setRequestId(recognizer.getLastRequestId());
                result.setResultType(ResultType.ERROR);
                result.setText("识别错误: " + e.getMessage());
                resultConsumer.accept(result);
            }
        };
    }

    /**
     * 读取音频数据并发送到识别服务
     */
    private void sendAudioData(InputStream inputStream, Recognition recognizer) throws IOException, InterruptedException {
        // 16000Hz采样率下，100ms的音频数据大小为3200字节
        byte[] buffer = new byte[3200];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            ByteBuffer byteBuffer;

            // 处理最后一个可能小于缓冲区大小的数据块
            if (bytesRead < buffer.length) {
                byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            } else {
                byteBuffer = ByteBuffer.wrap(buffer);
            }

            // 发送音频帧
            recognizer.sendAudioFrame(byteBuffer);

            // 重置缓冲区
            buffer = new byte[3200];

            // 模拟实时发送，每100ms发送一次
            Thread.sleep(100);
        }
    }

    /**
     * 停止指定的识别任务
     */
    public ASRResult stopRecognition(String taskId) {
        ASRResult result = new ASRResult();
        result.setTaskId(taskId);

        Recognition recognizer = runningTasks.get(taskId);
        if (recognizer == null) {
            result.setResultType(ResultType.ERROR);
            result.setText("任务不存在或已完成");
            return result;
        }

        try {
            recognizer.stop();
            recognizer.getDuplexApi().close(1000, "用户主动停止");
            result.setResultType(ResultType.COMPLETE);
            result.setText("任务已停止");
        } catch (Exception e) {
            result.setResultType(ResultType.ERROR);
            result.setText("停止任务出错: " + e.getMessage());
        } finally {
            runningTasks.remove(taskId);
        }

        return result;
    }

    /**
     * 清理任务资源
     */
    public void cleanupTask(String taskId) {
        Recognition recognizer = runningTasks.remove(taskId);
        if (recognizer != null) {
            try {
                recognizer.getDuplexApi().close(1000, "连接关闭，清理任务");
            } catch (Exception e) {
                // 忽略关闭时的异常
            }
        }
    }
}
