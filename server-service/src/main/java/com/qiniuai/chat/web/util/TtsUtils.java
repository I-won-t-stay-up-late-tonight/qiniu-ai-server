package com.qiniuai.chat.web.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流处理工具类
 * 提供输入流转字节数组等常用操作
 */
public class TtsUtils {

    /**
     * 将输入流转换为字节数组
     * @param inputStream 输入流（需外部保证关闭，或使用try-with-resources）
     * @return 字节数组
     * @throws IOException 流操作异常
     */
    public static byte[] readStream(InputStream inputStream) throws IOException {
        // 校验输入流不为空
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为null");
        }

        // 字节数组输出流（用于临时存储流数据）
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // 缓冲区（1KB，可根据需求调整）
        int length;

        // 循环读取输入流到缓冲区
        while ((length = inputStream.read(buffer)) != -1) {
            // 将缓冲区数据写入输出流
            outputStream.write(buffer, 0, length);
        }

        // 刷新输出流，确保所有数据写入
        outputStream.flush();

        // 将输出流转换为字节数组并返回
        return outputStream.toByteArray();
    }
}