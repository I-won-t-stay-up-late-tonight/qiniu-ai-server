package com.qiniuai.chat.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;
import com.qiniuai.chat.web.entity.Result.ChatResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName audioService
 * @Description TODO
 * @Author IFundo
 * @Date 00:05 2025/9/23
 * @Version 1.0
 */


public interface AudioService {

    String audio2text(MultipartFile audio);
    String text2audio(String content);
    ChatResult chat(String content, long conversationId, String url);
    String audioChat(MultipartFile audio, long conversationId);
    String chatByContent(String content, long conversationId);
}
