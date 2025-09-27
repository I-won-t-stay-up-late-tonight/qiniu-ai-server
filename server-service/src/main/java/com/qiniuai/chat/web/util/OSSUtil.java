package com.qiniuai.chat.web.util;

/**
 * @ClassName OSSUtil
 * @Description TODO
 * @Author IFundo
 * @Date 13:32 2025/9/27
 * @Version 1.0
 */

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.qiniuai.chat.web.config.OssConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
public class OSSUtil {
    // 阿里云OSS配置信息

    /**
     * 上传MultipartFile到OSS
     * @param file 文件
     * @param folderName 文件夹名称
     * @return OSS URL
     */
    public static String uploadMultipartFile(MultipartFile file, String folderName) {
        try {
            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + extension;

            // 按日期分类存储
            String datePath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String objectName = folderName + datePath + "/" + fileName;

            // 创建OSSClient
            OSS ossClient = new OSSClientBuilder()
                    .build(OssConfig.getEndpoint(), OssConfig.getAccessKeyId(), OssConfig.getAccessKeySecret());

            // 上传文件
            PutObjectRequest putRequest = new PutObjectRequest(
                    OssConfig.getBucketName(),
                    objectName,
                    file.getInputStream());
            PutObjectResult result = ossClient.putObject(putRequest);

            // 关闭OSSClient
            ossClient.shutdown();

            // 返回URL
            return buildUrl(objectName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传本地文件到OSS
     * @param file 本地文件
     * @param folderName 文件夹名称
     * @return OSS URL
     */
    public static String uploadLocalFile(File file, String folderName) {
        // 生成唯一文件名
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String uniqueName = UUID.randomUUID() + extension;

        // 按日期分类存储
        String datePath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String objectName = folderName + datePath + "/" + uniqueName;

        // 创建OSSClient
        OSS ossClient = new OSSClientBuilder()
                .build(OssConfig.getEndpoint(), OssConfig.getAccessKeyId(), OssConfig.getAccessKeySecret());

        // 上传文件
        PutObjectRequest putRequest = new PutObjectRequest(
                OssConfig.getBucketName(),
                objectName,
                file);
        PutObjectResult result = ossClient.putObject(putRequest);

        // 关闭OSSClient
        ossClient.shutdown();

        // 返回URL
        return buildUrl(objectName);
    }

    /**
     * 上传输入流到OSS
     * @param inputStream 输入流
     * @param folderName 文件夹名称
     * @param extension 文件扩展名
     * @return OSS URL
     */
    public static String uploadInputStream(InputStream inputStream, String folderName, String extension) {
        // 生成唯一文件名
        String fileName = UUID.randomUUID() + extension;

        // 按日期分类存储
        String datePath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String objectName = folderName + datePath + "/" + fileName;

        // 创建OSSClient
        OSS ossClient = new OSSClientBuilder()
                .build(OssConfig.getEndpoint(), OssConfig.getAccessKeyId(), OssConfig.getAccessKeySecret());

        // 上传文件
        PutObjectRequest putRequest = new PutObjectRequest(
                OssConfig.getBucketName(),
                objectName,
                inputStream);
        PutObjectResult result = ossClient.putObject(putRequest);

        // 关闭OSSClient
        ossClient.shutdown();

        // 返回URL
        return buildUrl(objectName);
    }

    /**
     * 构建文件URL
     * @param objectName OSS对象名称
     * @return 完整URL
     */
    private static String buildUrl(String objectName) {
        if (OssConfig.getCustomDomain() != null && !OssConfig.getCustomDomain().isEmpty()) {
            return OssConfig.getCustomDomain() + "/" + objectName;
        } else {
            return "https://" + OssConfig.getBucketName() + "." +
                    OssConfig.getEndpoint().substring(OssConfig.getEndpoint().indexOf("//") + 2) + "/" + objectName;
        }
    }
}
