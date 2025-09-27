package com.qiniuai.chat.web.config;

/**
 * @ClassName OssConfig
 * @Description TODO
 * @Author IFundo
 * @Date 14:03 2025/9/27
 * @Version 1.0
 */
public class OssConfig {
    // 替换为你的阿里云OSS配置
    private static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com"; // 地域节点
    private static final String ACCESS_KEY_ID = "LTAI5tMmpMphyx8YaDADAUhr"; // AccessKey ID
    private static final String ACCESS_KEY_SECRET = "to6w8cA58mGztKA200CI9cixnq5WKq"; // AccessKey Secret
    private static final String BUCKET_NAME = "qiniuai"; // 存储桶名称
    private static final String CUSTOM_DOMAIN = "https://qiniuai.oss-cn-beijing.aliyuncs.com"; // 自定义域名（可选）

    // Getter方法
    public static String getEndpoint() { return ENDPOINT; }
    public static String getAccessKeyId() { return ACCESS_KEY_ID; }
    public static String getAccessKeySecret() { return ACCESS_KEY_SECRET; }
    public static String getBucketName() { return BUCKET_NAME; }
    public static String getCustomDomain() { return CUSTOM_DOMAIN; }
}
