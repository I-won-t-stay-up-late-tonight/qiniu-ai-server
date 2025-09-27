package com.qiniuai.chat.audiochat.helper.alibaba.asr;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionResult;
import com.alibaba.dashscope.common.ResultCallback;
import com.hnit.server.utils.TimeUtils;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 识别语音文件
 * 文档：https://help.aliyun.com/zh/model-studio/paraformer-real-time-speech-recognition-java-sdk?spm=a2c4g.11186623.0.0.767769ce9wWdZD#71aa9440f9f9e
 */

class AudioTextRealtimeRecognitionTask implements Runnable {
    private Path filepath;

    public AudioTextRealtimeRecognitionTask(Path filepath) {
        this.filepath = filepath;
    }

    @Override
    public void run() {
        RecognitionParam param = RecognitionParam.builder()
                // 若没有将API Key配置到环境变量中，需将apiKey替换为自己的API Key
                // .apiKey("yourApikey")
                .model("paraformer-realtime-v2")
                .format("wav")
                .sampleRate(16000)
                // “language_hints”只支持paraformer-realtime-v2模型
                .parameter("language_hints", new String[]{"zh", "en"})
                .build();
        Recognition recognizer = new Recognition();

        String threadName = Thread.currentThread().getName();

        ResultCallback<RecognitionResult> callback = new ResultCallback<RecognitionResult>() {
            @Override
            public void onEvent(RecognitionResult message) {
                if (message.isSentenceEnd()) {

                    System.out.println(TimeUtils.getTimestamp()+" "+
                            "[process " + threadName + "] Final Result:" + message.getSentence().getText());
                } else {
                    System.out.println(TimeUtils.getTimestamp()+" "+
                            "[process " + threadName + "] Intermediate Result: " + message.getSentence().getText());
                }
            }

            @Override
            public void onComplete() {
                System.out.println(TimeUtils.getTimestamp()+" "+"[" + threadName + "] Recognition complete");
            }

            @Override
            public void onError(Exception e) {
                System.out.println(TimeUtils.getTimestamp()+" "+
                        "[" + threadName + "] RecognitionCallback error: " + e.getMessage());
            }
        };

        try {
            recognizer.call(param, callback);
            // Please replace the path with your audio file path
            System.out.println(TimeUtils.getTimestamp()+" "+"[" + threadName + "] Input file_path is: " + this.filepath);
            // Read file and send audio by chunks
            FileInputStream fis = new FileInputStream(this.filepath.toFile());
            // chunk size set to 1 seconds for 16KHz sample rate
            byte[] buffer = new byte[3200];
            int bytesRead;
            // Loop to read chunks of the file
            while ((bytesRead = fis.read(buffer)) != -1) {
                ByteBuffer byteBuffer;
                // Handle the last chunk which might be smaller than the buffer size
                System.out.println(TimeUtils.getTimestamp()+" "+"[" + threadName + "] bytesRead: " + bytesRead);
                if (bytesRead < buffer.length) {
                    byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                } else {
                    byteBuffer = ByteBuffer.wrap(buffer);
                }

                recognizer.sendAudioFrame(byteBuffer);
                buffer = new byte[3200];
                Thread.sleep(100);
            }
            System.out.println(TimeUtils.getTimestamp()+" "+LocalDateTime.now());
            recognizer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 任务结束后关闭 Websocket 连接
            recognizer.getDuplexApi().close(1000, "bye");
        }

        System.out.println(
                "["
                        + threadName
                        + "][Metric] requestId: "
                        + recognizer.getLastRequestId()
                        + ", first package delay ms: "
                        + recognizer.getFirstPackageDelay()
                        + ", last package delay ms: "
                        + recognizer.getLastPackageDelay());
    }


    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new AudioTextRealtimeRecognitionTask(Paths.get(System.getProperty("user.dir"), "synthesized-speech.wav")));
        executorService.shutdown();

        // wait for all tasks to complete
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        System.exit(0);
    }
}