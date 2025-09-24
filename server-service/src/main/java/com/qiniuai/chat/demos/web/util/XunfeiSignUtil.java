package com.qiniuai.chat.demos.web.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 讯飞TTS签名工具类（生成v2接口所需的HMAC-SHA256签名）
 */
public class XunfeiSignUtil {
    // 加密算法（v2接口固定为HmacSHA256）
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * 生成签名
     * @param apiSecret 讯飞API Secret
     * @param host 接口主机名（固定为tts-api.xfyun.cn）
     * @param date RFC1123格式日期（如：Wed, 24 Sep 2025 11:58:38 GMT）
     * @param requestLine 请求行（v2接口固定为GET /v2/tts HTTP/1.1）
     * @return Base64编码后的签名
     */
    public static String generateSign(String apiSecret, String host, String date, String requestLine) throws NoSuchAlgorithmException, InvalidKeyException {
        // 1. 构建待签字符串（v2接口固定格式：host\n日期\n请求行）
        String signStr = String.format("%s\n%s\n%s", host, date, requestLine);

        // 2. HMAC-SHA256加密
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        mac.init(secretKey);
        byte[] signBytes = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));

        // 3. Base64编码（v2接口要求）
        return Base64.getEncoder().encodeToString(signBytes);
    }
}