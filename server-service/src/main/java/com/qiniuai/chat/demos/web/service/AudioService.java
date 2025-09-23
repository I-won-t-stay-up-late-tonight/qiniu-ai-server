package com.qiniuai.chat.demos.web.service;

//import com.alibaba.dashscope.exception.NoApiKeyException;
//import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @ClassName audioService
 * @Description TODO
 * @Author IFundo
 * @Date 00:05 2025/9/23
 * @Version 1.0
 */


public interface AudioService {

    String audio2text(MultipartFile audio) throws ApiException, NoApiKeyException, UploadFileException, IOException;
    String text2audio(String content) throws ApiException, NoApiKeyException, UploadFileException ;

}
