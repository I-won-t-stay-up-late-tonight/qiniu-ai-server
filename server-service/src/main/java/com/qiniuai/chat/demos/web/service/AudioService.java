package com.qiniuai.chat.demos.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;
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
    String chat(String content, long id);
    String audioChat(MultipartFile audio, long id);

}
