package com.qiniuai.chat.web.config;

import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 七牛云Kodo对象存储配置类
 */
@Configuration
public class QiniuKodoConfig {

    @Value("${qiniu.kodo.access-key}")
    private String accessKey;

    @Value("${qiniu.kodo.secret-key}")
    private String secretKey;

    @Value("${qiniu.kodo.bucket-name}")
    private String bucketName;

    @Value("${qiniu.kodo.domain}")
    private String domain;

    /**
     * 初始化七牛云认证对象
     */
    @Bean
    public Auth qiniuAuth() {
        return Auth.create(accessKey, secretKey);
    }

    /**
     * 初始化七牛云上传管理器
     */
    @Bean
    public UploadManager uploadManager() {
        // 配置上传区域，根据自己的存储空间区域选择
        com.qiniu.storage.Configuration cfg = new com.qiniu.storage.Configuration(Region.huadong());
        return new UploadManager(cfg);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getDomain() {
        return domain;
    }
}
