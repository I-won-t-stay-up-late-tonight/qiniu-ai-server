package com.qiniuai.chat.audiochat.utils;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 音频处理工具类，负责PCM音频转WAV格式
 */
public class AudioUtils {

    /**
     * 将PCM音频数据转换为WAV格式
     * @param pcmData PCM原始音频数据
     * @param sampleRate 采样率（如24000）
     * @return WAV格式的音频字节数组
     * @throws IOException  IO异常
     * @throws LineUnavailableException 音频线路不可用异常
     */
    public static byte[] convertPcmToWav(byte[] pcmData, int sampleRate) throws IOException, LineUnavailableException {
        // 定义PCM音频格式（16位单声道，小端模式）
        AudioFormat pcmFormat = new AudioFormat(
                sampleRate,        // 采样率
                16,                // 位深度
                1,                 // 声道数（单声道）
                true,              // 是否有符号
                false              // 字节顺序（false表示小端）
        );

        // 将PCM字节数组包装为音频输入流
        ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
        AudioInputStream pcmAudioStream = new AudioInputStream(bais, pcmFormat, pcmData.length / pcmFormat.getFrameSize());

        // 输出WAV格式到字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 关键修正：正确的参数顺序为（音频输入流, 格式类型, 输出流）
        AudioSystem.write(pcmAudioStream, AudioFileFormat.Type.WAVE, baos);

        // 关闭流
        pcmAudioStream.close();
        baos.close();

        return baos.toByteArray();
    }
}

